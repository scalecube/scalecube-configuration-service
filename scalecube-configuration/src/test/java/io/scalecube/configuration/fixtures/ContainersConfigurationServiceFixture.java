package io.scalecube.configuration.fixtures;

import com.couchbase.client.java.cluster.DefaultBucketSettings;
import com.couchbase.client.java.cluster.UserRole;
import com.couchbase.client.java.cluster.UserSettings;
import com.github.dockerjava.api.model.PortBinding;
import io.scalecube.services.gateway.clientsdk.Client;
import io.scalecube.test.fixtures.Fixture;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.opentest4j.TestAbortedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.couchbase.CouchbaseContainer;
import org.testcontainers.vault.VaultContainer;

public class ContainersConfigurationServiceFixture implements Fixture {

  private static final Logger logger = LoggerFactory
      .getLogger(ContainersConfigurationServiceFixture.class);

  private static final String COUCHBASE_DOCKER_IMAGE = "couchbase:community-6.0.0";
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
  private static final int HTTP_GATEWAY_PORT = 8080;
  private static final int RS_GATEWAY_PORT = 9090;
  private static final String GATEWAY_NETWORK_ALIAS = "gateway";

  private static final String BUCKET_FULL_ACCESS = "bucket_full_access";

  //  private Environment environment;
  private Client client;

  @Override
  public void setUp() throws TestAbortedException {
    setUpCouchbase();
    setUpVault();
    setUpGateway();

    Map<String, String> env = new HashMap<>();
    env.put("VAULT_ADDR", String.format(VAULT_ADDR_PATTERN, VAULT_NETWORK_ALIAS, VAULT_PORT));
    env.put("VAULT_SECRETS_PATH", VAULT_SECRETS_PATH);
    env.put("VAULT_TOKEN", VAULT_TOKEN);

    setUpOrganizationService(env);
    setUpConfigurationService(env);

//    ClientSettings clientSettings = ClientSettings.builder().host("localhost").port(7070).build();
//
//    ClientCodec clientCodec = new WebsocketClientCodec(
//        DataCodec.getInstance(clientSettings.contentType()));
//
//    ClientTransport transport = new WebsocketClientTransport(
//        clientSettings, clientCodec, LoopResources.create("ws" + "-loop"));
//
//    client = new Client(transport, clientCodec);
  }

  @Override
  public <T> T proxyFor(Class<? extends T> clasz) {
    return client.forService(clasz);
  }

  @Override
  public void tearDown() {
  }

  private void setUpOrganizationService(Map<String, String> env) {
    env.put("JAVA_OPTS", "-Dio.scalecube.organization.seeds=" + GATEWAY_NETWORK_ALIAS + ":4801");

    new GenericContainer<>("scalecube/scalecube-organization:latest")
        .withNetwork(Network.SHARED)
        .withNetworkAliases("scalecube-organization")
        .withCreateContainerCmdModifier(cmd -> cmd.withName("scalecube-organization"))
        .withEnv(env)
        .start();
  }

  private void setUpConfigurationService(Map<String, String> env) {
    env.put("JAVA_OPTS", "-Dio.scalecube.configuration.seeds=" + GATEWAY_NETWORK_ALIAS + ":4801");

    new GenericContainer<>("scalecube/scalecube-configuration:latest")
        .withNetwork(Network.SHARED)
        .withNetworkAliases("scalecube-configuration")
        .withCreateContainerCmdModifier(cmd -> cmd.withName("scalecube-configuration"))
        .withEnv(env)
        .waitingFor(new LogMessageWaitStrategy().withRegEx("^.*scalecube.*Running.*$"))
        .start();
  }

  private void setUpGateway() {
    GenericContainer gateway =
        new GenericContainer<>("scalecube/scalecube-services-gateway-runner:2.5.3")
            .withExposedPorts(WS_GATEWAY_PORT, HTTP_GATEWAY_PORT, RS_GATEWAY_PORT)
            .withNetwork(Network.SHARED)
            .withNetworkAliases(GATEWAY_NETWORK_ALIAS)
            .withCreateContainerCmdModifier(
                cmd -> {
                  cmd.withName(GATEWAY_NETWORK_ALIAS);
                  cmd.withPortBindings(
                      PortBinding.parse(WS_GATEWAY_PORT + ":" + WS_GATEWAY_PORT),
                      PortBinding.parse(HTTP_GATEWAY_PORT + ":" + HTTP_GATEWAY_PORT),
                      PortBinding.parse(RS_GATEWAY_PORT + ":" + RS_GATEWAY_PORT));
                })
            .waitingFor(new HostPortWaitStrategy());
    gateway.start();
  }

  private void setUpVault() {
    VaultContainer<?> vault =
        new VaultContainer<>(VAULT_DOCKER_IMAGE)
            .withVaultPort(VAULT_PORT)
            .withVaultToken(VAULT_TOKEN)
            .withSecretInVault(VAULT_SECRETS_PATH,
                "couchbase.hosts=" + COUCHBASE_NETWORK_ALIAS,
                "couchbase.username=" + COUCHBASE_USERNAME,
                "couchbase.password=" + COUCHBASE_PASSWORD)
            .withNetwork(Network.SHARED)
            .withNetworkAliases(VAULT_NETWORK_ALIAS)
            .withCreateContainerCmdModifier(cmd -> cmd.withName(VAULT_NETWORK_ALIAS))
            .waitingFor(new LogMessageWaitStrategy().withRegEx("^.*Vault server started!.*$"));
    vault.start();
  }


  private void setUpCouchbase() {
    CouchbaseContainer couchbase =
        new CouchbaseContainer(COUCHBASE_DOCKER_IMAGE)
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
    try {
      couchbase.callCouchbaseRestAPI("/settings/indexes", "storageMode=forestdb");
    } catch (IOException e) {
      logger.warn("Couchbase set up issues", e);
    }

    String password = "123456";
    createBucket(couchbase, "organizations", password);
    createBucket(couchbase, "configurations", password);
  }

  private void createBucket(CouchbaseContainer couchbase, String name, String password) {
    couchbase.createBucket(
        DefaultBucketSettings.builder().name(name).password(password).build(),
        UserSettings.build()
            .name(name)
            .password(password)
            .roles(Collections.singletonList(new UserRole(BUCKET_FULL_ACCESS, name))),
        true);
  }
}
