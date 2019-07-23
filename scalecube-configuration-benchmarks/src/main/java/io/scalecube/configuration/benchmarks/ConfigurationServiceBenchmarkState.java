package io.scalecube.configuration.benchmarks;

import static io.scalecube.services.gateway.transport.GatewayClientTransports.HTTP_CLIENT_CODEC;
import static io.scalecube.services.gateway.transport.GatewayClientTransports.RSOCKET_CLIENT_CODEC;
import static io.scalecube.services.gateway.transport.GatewayClientTransports.WEBSOCKET_CLIENT_CODEC;

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
import io.scalecube.configuration.api.CreateOrUpdateEntryRequest;
import io.scalecube.configuration.api.CreateRepositoryRequest;
import io.scalecube.net.Address;
import io.scalecube.services.ServiceCall;
import io.scalecube.services.gateway.transport.GatewayClient;
import io.scalecube.services.gateway.transport.GatewayClientSettings;
import io.scalecube.services.gateway.transport.GatewayClientSettings.Builder;
import io.scalecube.services.gateway.transport.GatewayClientTransport;
import io.scalecube.services.gateway.transport.StaticAddressRouter;
import io.scalecube.services.gateway.transport.http.HttpGatewayClient;
import io.scalecube.services.gateway.transport.rsocket.RSocketGatewayClient;
import io.scalecube.services.gateway.transport.websocket.WebsocketGatewayClient;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

final class ConfigurationServiceBenchmarkState
    extends BenchmarkState<ConfigurationServiceBenchmarkState> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(ConfigurationServiceBenchmarkState.class);

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final boolean useTestContainers;
  private final String gatewayHost;
  private final int gatewayPort;
  private final String gatewayProtocol;
  private final boolean secure;

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
    secure = Boolean.valueOf(settings.find("secure", "false"));
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
  private GatewayClient client() {
    Builder settingsBuilder = GatewayClientSettings.builder().host(gatewayHost).port(gatewayPort);

    if (secure) {
      settingsBuilder.secure();
    }

    GatewayClientSettings settings = settingsBuilder.build();

    GatewayClient client;

    switch (gatewayProtocol.toLowerCase()) {
      case "ws":
        client = new WebsocketGatewayClient(settings, WEBSOCKET_CLIENT_CODEC);
        break;
      case "rs":
        client = new RSocketGatewayClient(settings, RSOCKET_CLIENT_CODEC);
        break;
      case "http":
        client = new HttpGatewayClient(settings, HTTP_CLIENT_CODEC);
        break;
      default:
        throw new IllegalStateException(
            String.format(
                "Unknown gateway protocol '%s'. Must be one of following 'ws', 'rs', 'http'",
                gatewayProtocol));
    }

    return client;
  }

  private GatewayClientTransport clientTransport() {
    return new GatewayClientTransport(client());
  }

  protected <T> T forService(Class<T> clazz) {
    return new ServiceCall()
        .transport(clientTransport())
        .router(new StaticAddressRouter(Address.create(gatewayHost, gatewayPort)))
        .api(clazz);
  }

  private void preload(Token token, int configKeysCount) {
    OrganizationService organizationService = forService(OrganizationService.class);
    ConfigurationService configurationService = forService(ConfigurationService.class);

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
        .createEntry(new CreateOrUpdateEntryRequest(apiKey, "benchmarks-repo", key, value))
        .doOnSuccess(response -> LOGGER.info("Config created: {}={}", key, value))
        .doOnError(th -> LOGGER.error("Config not created: ", th))
        .then();
  }
}
