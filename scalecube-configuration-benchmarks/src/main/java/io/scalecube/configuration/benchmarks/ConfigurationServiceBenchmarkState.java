package io.scalecube.configuration.benchmarks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.scalecube.account.api.AddOrganizationApiKeyRequest;
import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.CreateOrganizationResponse;
import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.benchmarks.BenchmarkSettings;
import io.scalecube.benchmarks.BenchmarkState;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.CreateRepositoryRequest;
import io.scalecube.configuration.api.SaveRequest;
import io.scalecube.services.gateway.clientsdk.Client;
import io.scalecube.services.gateway.clientsdk.ClientSettings;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.resources.LoopResources;

final class ConfigurationServiceBenchmarkState
    extends BenchmarkState<ConfigurationServiceBenchmarkState> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(ConfigurationServiceBenchmarkState.class);

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
    gatewayPort = Integer.valueOf(settings.find("gatewayPort", "7070"));
    gatewayProtocol = String.valueOf(settings.find("gatewayProtocol", "ws"));
  }

  @Override
  public void beforeAll() {
    if (useTestContainers) {
      new Environment().start();
    }

    Token token = new Token(settings.find("token", ""));
    int configKeysCount = Integer.parseInt(settings.find("configKeysCount", "100"));

    preload(token, configKeysCount);
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
        return Client.websocket(settings);
      case "rs":
        return Client.rsocket(settings);
      case "http":
        return Client.http(settings);
      default:
        throw new IllegalStateException(
            String.format(
                "Unknown gateway protocol '%s'. Must be one of following 'ws', 'rs', 'http'",
                gatewayProtocol));
    }
  }

  private void preload(Token token, int configKeysCount) {
    Client client = client();
    OrganizationService organizationService = client.forService(OrganizationService.class);
    ConfigurationService configurationService = client.forService(ConfigurationService.class);

    createOrganization(organizationService, token)
        .flatMap(organization -> createApiKey(organizationService, token, organization))
        .flatMapMany(
            apiKey ->
                createRepository(configurationService, apiKey)
                    .thenMany(
                        Flux.range(0, configKeysCount)
                            .flatMap(
                                keyIndex -> {
                                  String key = "key-" + keyIndex;
                                  JsonNode value = OBJECT_MAPPER.valueToTree(keyIndex);

                                  return saveConfigProperty(
                                      configurationService, apiKey, key, value);
                                })))
        .doOnComplete(() -> LOGGER.info("Preloading completed!"))
        .doOnError(th -> LOGGER.error("Preloading failed!", th))
        .blockLast();
  }

  private Mono<CreateOrganizationResponse> createOrganization(
      OrganizationService organizationService, Token token) {
    return organizationService
        .createOrganization(new CreateOrganizationRequest("benchmarks", "info@scalecube.io", token))
        .doOnSuccess(response -> LOGGER.info("Organization created: {}", response))
        .doOnError(th -> LOGGER.error("Organization not created", th));
  }

  private Mono<String> createApiKey(
      OrganizationService organizationService, Token token, OrganizationInfo organization) {
    Map<String, String> claims = new HashMap<>();
    claims.put("role", Role.Owner.name());
    return organizationService
        .addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(token, organization.id(), "benchmarksApiKey", claims))
        .doOnSuccess(response -> LOGGER.info("ApiKey created: {}", response))
        .doOnError(th -> LOGGER.error("ApiKey not created", th))
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
