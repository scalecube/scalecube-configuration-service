package io.scalecube.configuration.benchmarks;

import io.scalecube.benchmarks.BenchmarkSettings;
import io.scalecube.benchmarks.BenchmarkState;
import io.scalecube.services.gateway.clientsdk.Client;
import io.scalecube.services.gateway.clientsdk.ClientSettings;
import java.util.HashMap;
import java.util.Map;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.couchbase.CouchbaseContainer;
import org.testcontainers.vault.VaultContainer;
import reactor.netty.resources.LoopResources;

public class ConfigurationServiceBenchmarkState
    extends BenchmarkState<ConfigurationServiceBenchmarkState> {

  private static final String COUCHBASE_USERNAME = "admin";
  private static final String COUCHBASE_PASSWORD = "123456";
  private static final String VAULT_DOCKER_IMAGE = "vault:0.9.5";
  private static final int VAULT_PORT = 8200;
  private static final String VAULT_TOKEN = "token_for_benchmarks";
  private static final String VAULT_SECRETS_PATH = "secret/configuration-service/dev";
  private static final LogMessageWaitStrategy VAULT_SERVER_STARTED =
      new LogMessageWaitStrategy().withRegEx("^.*Vault server started!.*$");
  private static final String VAULT_ADDR_PATTERN = "http://%s:%d";
  private static final int WS_GATEWAY_PORT = 7070;

  private boolean useTestContainers;

  /**
   * State.
   *
   * @param settings settings.
   */
  public ConfigurationServiceBenchmarkState(BenchmarkSettings settings) {
    super(settings);

    useTestContainers = Boolean.valueOf(settings.find("useTestContainers", "true"));
  }

  @Override
  public void beforeAll() {
    if (useTestContainers) {
      CouchbaseContainer couchbase = startCouchbase();
      VaultContainer vault = startVault(couchbase.getContainerIpAddress());
      GenericContainer gateway = startGateway();

      String vaultAddress =
          String.format(VAULT_ADDR_PATTERN, getContainerIpAddress(vault), VAULT_PORT);
      String gatewayAddress = getContainerIpAddress(gateway) + ":4801";

      Map<String, String> env = new HashMap<>();
      env.put("VAULT_ADDR", vaultAddress);
      env.put("VAULT_SECRETS_PATH", VAULT_SECRETS_PATH);
      env.put("VAULT_TOKEN", VAULT_TOKEN);

      startOrganizationService(env, gatewayAddress);
      startConfigurationService(env, gatewayAddress);
    }
  }

  public <T> T clientFor(Class<T> service) {
    return Client.onWebsocket(
            ClientSettings.builder()
                .port(WS_GATEWAY_PORT)
                .loopResources(LoopResources.create("benchmark-client"))
                .build())
        .forService(service);
  }

  private CouchbaseContainer startCouchbase() {
    CouchbaseContainer couchbase =
        new CouchbaseContainer()
            .withClusterAdmin(COUCHBASE_USERNAME, COUCHBASE_PASSWORD)
            .withCreateContainerCmdModifier(cmd -> cmd.withName("couchbase"));
    couchbase.start();
    couchbase.initCluster();
    return couchbase;
  }

  private VaultContainer startVault(String couchbaseHosts) {
    VaultContainer<?> vault =
        new VaultContainer<>(VAULT_DOCKER_IMAGE)
            .withVaultPort(VAULT_PORT)
            .withVaultToken(VAULT_TOKEN)
            .withSecretInVault(
                VAULT_SECRETS_PATH,
                "couchbase.hosts=" + couchbaseHosts,
                "couchbase.username=" + COUCHBASE_USERNAME,
                "couchbase.password=" + COUCHBASE_PASSWORD)
            .withCreateContainerCmdModifier(cmd -> cmd.withName("vault"))
            .waitingFor(VAULT_SERVER_STARTED);
    vault.start();
    return vault;
  }

  private GenericContainer startGateway() {
    GenericContainer gateway =
        new GenericContainer<>("scalecube/scalecube-services-gateway-runner:2.4.10")
            .withExposedPorts(7070)
            .withCreateContainerCmdModifier(cmd -> cmd.withName("gateway"));
    gateway.start();
    return gateway;
  }

  private void startOrganizationService(Map<String, String> env, String gatewayAddress) {
    env.put("JAVA_OPTS", " -Dio.scalecube.organization.seeds=" + gatewayAddress);

    new GenericContainer<>("scalecube/scalecube-organization:latest")
        .withCreateContainerCmdModifier(cmd -> cmd.withName("scalecube-organization"))
        .withEnv(env)
        .start();
  }

  private void startConfigurationService(Map<String, String> env, String gatewayAddress) {
    env.put("JAVA_OPTS", " -Dio.scalecube.configuration.seeds=" + gatewayAddress);

    new GenericContainer<>("scalecube/scalecube-configuration:latest")
        .withCreateContainerCmdModifier(cmd -> cmd.withName("scalecube-configuration"))
        .withEnv(env)
        .start();
  }

  private String getContainerIpAddress(GenericContainer container) {
    return container
        .getCurrentContainerInfo()
        .getNetworkSettings()
        .getNetworks()
        .get("bridge")
        .getIpAddress();
  }
}
