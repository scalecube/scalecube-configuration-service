package io.scalecube.configuration.benchmarks;

import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.bucket.AsyncBucketManager;
import com.couchbase.client.java.cluster.DefaultBucketSettings;
import com.couchbase.client.java.cluster.UserRole;
import com.couchbase.client.java.cluster.UserSettings;
import com.couchbase.client.java.document.JsonArrayDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.view.DefaultView;
import com.couchbase.client.java.view.DesignDocument;
import com.couchbase.client.java.view.DesignDocument.Option;
import com.github.dockerjava.api.model.PortBinding;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.couchbase.CouchbaseContainer;
import org.testcontainers.vault.VaultContainer;
import reactor.core.publisher.Mono;
import rx.RxReactiveStreams;

final class Environment {

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

  public static void main(String[] args) throws InterruptedException {
    new Environment().start();
    Thread.currentThread().join();
  }

  /** Starts environment. */
  public void start() {
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

  private void startCouchbase() {
    String name = "organizations";
    String configName = "configurations";
    String password = "123456";

    CouchbaseContainer couchbase =
        new CouchbaseContainer(COUCHBASE_DOCKER_IMAGE)
            .withClusterAdmin(COUCHBASE_USERNAME, COUCHBASE_PASSWORD)
            .withNetwork(Network.SHARED)
            .withNetworkAliases(COUCHBASE_NETWORK_ALIAS)
            .withNewBucket(
                DefaultBucketSettings.builder().name(name).password(password).build(),
                UserSettings.build()
                    .name(name)
                    .password(password)
                    .roles(Collections.singletonList(new UserRole(BUCKET_FULL_ACCESS, name))))
            .withNewBucket(
                DefaultBucketSettings.builder().name(configName).password(password).build(),
                UserSettings.build()
                    .name(configName)
                    .password(password)
                    .roles(Collections.singletonList(new UserRole(BUCKET_FULL_ACCESS, configName))))
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
      // ignore
    }

    couchbaseInit(couchbase, configName);
  }

  private static void couchbaseInit(CouchbaseContainer couchbase, String bucketName) {
    AsyncBucket bucket =
        couchbase.getCouchbaseCluster().openBucket(bucketName, COUCHBASE_PASSWORD).async();
    Mono.from(
            RxReactiveStreams.toPublisher(
                bucket.insert(
                    JsonArrayDocument.create("repos", JsonArray.create()), 100, TimeUnit.SECONDS)))
        .retryBackoff(100, Duration.ofSeconds(1), Duration.ofSeconds(5))
        .block();
    AsyncBucketManager bucketManager = bucket.bucketManager().toBlocking().first();

    Map<Option, Long> options = new HashMap<>();
    options.put(Option.UPDATE_MIN_CHANGES, 1L);
    options.put(Option.REPLICA_UPDATE_MIN_CHANGES, 1L);

    DesignDocument designDoc =
        DesignDocument.create(
            "keys",
            Arrays.asList(
                DefaultView.create(
                    "by_keys",
                    "function (doc, meta) { "
                        + "  if (meta.id != 'repos') { "
                        + "    emit(meta.id.substring(0, meta.id.lastIndexOf('::')), null);"
                        + "  }"
                        + "}")),
            options);

    bucketManager.insertDesignDocument(designDoc).toBlocking().first();
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
            .waitingFor(new LogMessageWaitStrategy().withRegEx("^.*Vault server started!.*$"));
    vault.start();
  }

  private void startGateway() {
    GenericContainer gateway =
        new GenericContainer<>("scalecube/scalecube-services-gateway-runner:2.8.4")
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

  private void startOrganizationService(Map<String, String> env) {
    env.put("JAVA_OPTS", "-Dio.scalecube.organization.seeds=" + GATEWAY_NETWORK_ALIAS + ":4801");

    new GenericContainer<>("scalecube/scalecube-organization:2.1.12")
        .withNetwork(Network.SHARED)
        .withNetworkAliases("scalecube-organization")
        .withCreateContainerCmdModifier(cmd -> cmd.withName("scalecube-organization"))
        .withEnv(env)
        .start();
  }

  private void startConfigurationService(Map<String, String> env) {
    env.put("JAVA_OPTS", "-Dio.scalecube.configuration.seeds=" + GATEWAY_NETWORK_ALIAS + ":4801");

    new GenericContainer<>("scalecube/scalecube-configuration:latest")
        .withNetwork(Network.SHARED)
        .withNetworkAliases("scalecube-configuration")
        .withCreateContainerCmdModifier(cmd -> cmd.withName("scalecube-configuration"))
        .withEnv(env)
        .waitingFor(new LogMessageWaitStrategy().withRegEx("^.*scalecube.*Running.*$"))
        .start();
  }
}
