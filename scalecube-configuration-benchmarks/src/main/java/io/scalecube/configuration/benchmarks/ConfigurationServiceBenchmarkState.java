package io.scalecube.configuration.benchmarks;

import com.couchbase.client.java.cluster.DefaultBucketSettings;
import com.couchbase.client.java.cluster.UserRole;
import com.couchbase.client.java.cluster.UserSettings;
import com.github.dockerjava.api.model.PortBinding;
import io.scalecube.benchmarks.BenchmarkSettings;
import io.scalecube.benchmarks.BenchmarkState;
import io.scalecube.services.gateway.clientsdk.Client;
import io.scalecube.services.gateway.clientsdk.ClientSettings;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.couchbase.CouchbaseContainer;
import org.testcontainers.vault.VaultContainer;
import reactor.netty.resources.LoopResources;

public class ConfigurationServiceBenchmarkState
    extends BenchmarkState<ConfigurationServiceBenchmarkState> {

  private static final String COUCHBASE_USERNAME = "admin";
  private static final String COUCHBASE_PASSWORD = "123456";
  private static final String COUCHBASE_NETWORK_ALIAS = "couchbase";

  private static final String VAULT_DOCKER_IMAGE = "vault:0.9.5";
  private static final int VAULT_PORT = 8200;
  private static final String VAULT_TOKEN = "token_for_benchmarks";
  private static final String VAULT_SECRETS_PATH = "secret/configuration-service/dev";
  private static final String VAULT_NETWORK_ALIAS = "vault";
  private static final String VAULT_ADDR_PATTERN = "http://%s:%d";

  private static final int WS_GATEWAY_PORT = 7070;
  private static final String GATEWAY_NETWORK_ALIAS = "gateway";

  private static final WaitStrategy VAULT_SERVER_STARTED =
      new LogMessageWaitStrategy().withRegEx("^.*Vault server started!.*$");
  private static final WaitStrategy SERVICE_STARTED =
      new LogMessageWaitStrategy().withRegEx("^.*scalecube.*Running.*$");

  private final boolean useTestContainers;

  /**
   * Creates new instance.
   *
   * @param settings benchmarks settings.
   */
  public ConfigurationServiceBenchmarkState(BenchmarkSettings settings) {
    super(settings);

    useTestContainers = Boolean.valueOf(settings.find("useTestContainers", "true"));
  }

  @Override
  public void beforeAll() {
    if (useTestContainers) {
      startCouchbase();
      startVault();
      startGateway();

      Map<String, String> env = new HashMap<>();
      env.put("VAULT_ADDR", String.format(VAULT_ADDR_PATTERN, VAULT_NETWORK_ALIAS, VAULT_PORT));
      env.put("VAULT_SECRETS_PATH", VAULT_SECRETS_PATH);
      env.put("VAULT_TOKEN", VAULT_TOKEN);

      startOrganizationService(env);
      startConfigurationService(env);
    }
  }

  /**
   * Creates service proxy with websocket client for a given service interface.
   *
   * @param service service interface class.
   * @param <T> service type.
   * @return service proxy.
   */
  public <T> T clientFor(Class<T> service) {
    return Client.onWebsocket(
            ClientSettings.builder()
                .port(WS_GATEWAY_PORT)
                .loopResources(LoopResources.create("benchmark-client"))
                .build())
        .forService(service);
  }

  private void startCouchbase() {
    CouchbaseContainer couchbase =
        new CouchbaseContainer()
            .withClusterAdmin(COUCHBASE_USERNAME, COUCHBASE_PASSWORD)
            .withNetwork(Network.SHARED)
            .withNetworkAliases(COUCHBASE_NETWORK_ALIAS)
            .withCreateContainerCmdModifier(
                cmd -> {
                  cmd.withName(COUCHBASE_NETWORK_ALIAS);
                  cmd.withPortBindings(PortBinding.parse("8091:8091"));
                });
    couchbase.start();
    couchbase.initCluster();

    String name = "organizations";
    String password = "123456";
    couchbase.createBucket(
        DefaultBucketSettings.builder().name(name).password(password).quota(100).build(),
        UserSettings.build()
            .name(name)
            .password(password)
            .roles(Collections.singletonList(new UserRole("bucket_full_access", name))),
        true);
  }

  private void startVault() {
    VaultContainer<?> vault =
        new VaultContainer<>(VAULT_DOCKER_IMAGE)
            .withVaultPort(VAULT_PORT)
            .withVaultToken(VAULT_TOKEN)
            .withSecretInVault(
                VAULT_SECRETS_PATH,
                "couchbase.hosts=" + COUCHBASE_NETWORK_ALIAS,
                "couchbase.username=" + COUCHBASE_USERNAME,
                "couchbase.password=" + COUCHBASE_PASSWORD)
            .withNetwork(Network.SHARED)
            .withNetworkAliases(VAULT_NETWORK_ALIAS)
            .withCreateContainerCmdModifier(cmd -> cmd.withName(VAULT_NETWORK_ALIAS))
            .waitingFor(VAULT_SERVER_STARTED);
    vault.start();
  }

  private void startGateway() {
    GenericContainer gateway =
        new GenericContainer<>("scalecube/scalecube-services-gateway-runner:2.4.10")
            .withExposedPorts(WS_GATEWAY_PORT)
            .withNetwork(Network.SHARED)
            .withNetworkAliases(GATEWAY_NETWORK_ALIAS)
            .withCreateContainerCmdModifier(
                cmd -> {
                  cmd.withName(GATEWAY_NETWORK_ALIAS);
                  cmd.withPortBindings(PortBinding.parse(WS_GATEWAY_PORT + ":" + WS_GATEWAY_PORT));
                })
            .waitingFor(new HostPortWaitStrategy());
    gateway.start();
  }

  private void startOrganizationService(Map<String, String> env) {
    env.put("JAVA_OPTS", "-Dio.scalecube.organization.seeds=" + GATEWAY_NETWORK_ALIAS + ":4801");

    new GenericContainer<>("scalecube/scalecube-organization:latest")
        .withNetwork(Network.SHARED)
        .withNetworkAliases("scalecube-organization")
        .withCreateContainerCmdModifier(cmd -> cmd.withName("scalecube-organization"))
        .withEnv(env)
        // TODO: uncomment after adding Logo to organization service
        // .waitingFor(SERVICE_STARTED)
        .start();
  }

  private void startConfigurationService(Map<String, String> env) {
    env.put("JAVA_OPTS", "-Dio.scalecube.configuration.seeds=" + GATEWAY_NETWORK_ALIAS + ":4801");

    new GenericContainer<>("scalecube/scalecube-configuration:latest")
        .withNetwork(Network.SHARED)
        .withNetworkAliases("scalecube-configuration")
        .withCreateContainerCmdModifier(cmd -> cmd.withName("scalecube-configuration"))
        .withEnv(env)
        .waitingFor(SERVICE_STARTED)
        .start();
  }
}
