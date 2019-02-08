package io.scalecube.configuration.benchmarks;

import com.couchbase.client.java.cluster.DefaultBucketSettings;
import com.couchbase.client.java.cluster.UserRole;
import com.couchbase.client.java.cluster.UserSettings;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.model.PortBinding;
import io.scalecube.account.api.AddOrganizationApiKeyRequest;
import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.CreateOrganizationResponse;
import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Token;
import io.scalecube.benchmarks.BenchmarkSettings;
import io.scalecube.benchmarks.BenchmarkState;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.CreateRepositoryRequest;
import io.scalecube.configuration.api.SaveRequest;
import io.scalecube.services.gateway.clientsdk.Client;
import io.scalecube.services.gateway.clientsdk.ClientSettings;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.couchbase.CouchbaseContainer;
import org.testcontainers.vault.VaultContainer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.resources.LoopResources;

final class ConfigurationServiceBenchmarkState
    extends BenchmarkState<ConfigurationServiceBenchmarkState> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(ConfigurationServiceBenchmarkState.class);

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
  private final String gatewayHost;
  private final int gatewayPort;
  private final String gatewayProtocol;

  private final AtomicReference<String> apiKey = new AtomicReference<>();

  /**
   * Creates new instance.
   *
   * @param settings benchmarks settings.
   */
  public ConfigurationServiceBenchmarkState(BenchmarkSettings settings) {
    super(settings);

    useTestContainers = Boolean.valueOf(settings.find("useTestContainers", "false"));
    gatewayHost = String.valueOf(settings.find("gatewayHost", "localhost"));
    gatewayPort = Integer.valueOf(settings.find("gatewayPort", String.valueOf(WS_GATEWAY_PORT)));
    gatewayProtocol = String.valueOf(settings.find("gatewayProtocol", "ws"));
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

    Token token = new Token("auth0.com", settings.find("token", ""));
    int configKeysCount = Integer.parseInt(settings.find("configKeysCount", "100"));

    preload(token, configKeysCount).block();
  }

  /**
   * Returns API key which is created on `preload` phase.
   *
   * @return api key.
   */
  public String apiKey() {
    return apiKey.get();
  }

  /**
   * Creates gateway client.
   *
   * @return client.
   */
  public Client client() {
    ClientSettings settings =
        ClientSettings.builder()
            .host(gatewayHost)
            .port(gatewayPort)
            .loopResources(LoopResources.create("benchmark-client"))
            .build();

    switch (gatewayProtocol.toLowerCase()) {
      case "ws":
        return Client.onWebsocket(settings);
      case "rs":
        return Client.onRSocket(settings);
      case "http":
        return Client.onHttp(settings);
      default:
        throw new IllegalStateException(
            String.format(
                "Unknown gateway protocol '%s'. Must be one of following 'ws', 'rs', 'http'",
                gatewayProtocol));
    }
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

  private Mono<Void> preload(Token token, int configKeysCount) {
    Client client = client();
    OrganizationService organizationService = client.forService(OrganizationService.class);
    ConfigurationService configurationService = client.forService(ConfigurationService.class);

    return createOrganization(organizationService, token)
        .flatMap(organization -> createApiKey(organizationService, token, organization))
        .flatMap(apiKey -> createRepository(configurationService, apiKey).then(Mono.just(apiKey)))
        .flatMapMany(
            apiKey ->
                Flux.range(0, configKeysCount)
                    .flatMap(
                        keyIndex -> {
                          String key = "key-" + keyIndex;
                          JsonNode value = OBJECT_MAPPER.valueToTree(keyIndex);

                          return saveConfigProperty(configurationService, apiKey, key, value);
                        }))
        .doOnComplete(() -> LOGGER.info("Preloading completed!"))
        .then();
  }

  private Mono<CreateOrganizationResponse> createOrganization(
      OrganizationService organizationService, Token token) {
    return organizationService
        .createOrganization(new CreateOrganizationRequest("benchmarks", token, "info@scalecube.io"))
        .doOnSuccess(response -> LOGGER.info("Organization created: {}", response))
        .doOnError(th -> LOGGER.error("Organization not created: {}", th));
  }

  private Mono<String> createApiKey(
      OrganizationService organizationService, Token token, OrganizationInfo organization) {
    Map<String, String> claims = new HashMap<>();
    claims.put("role", "Owner");
    return organizationService
        .addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(token, organization.id(), "benchmarksApiKey", claims))
        .doOnSuccess(response -> LOGGER.info("ApiKey created: {}", response))
        .doOnError(th -> LOGGER.error("ApiKey not created: {}", th))
        .map(response -> response.apiKeys()[0].key())
        .doOnNext(apiKey::set);
  }

  private Mono<Void> createRepository(ConfigurationService configurationService, String apiKey) {
    return configurationService
        .createRepository(new CreateRepositoryRequest(apiKey, "benchmarks-repo"))
        .doOnSuccess(response -> LOGGER.info("Repository created: {}", response))
        .doOnError(th -> LOGGER.error("Repository not created: ", th))
        .then();
  }

  private Mono<Void> saveConfigProperty(
      ConfigurationService configurationService, String apiKey, String key, JsonNode value) {
    return configurationService
        .save(new SaveRequest(apiKey, "benchmarks-repo", key, value))
        .doOnSuccess(response -> LOGGER.info("Config created: {}={}", key, value))
        .doOnError(th -> LOGGER.error("Config not created: ", th))
        .then();
  }
}
