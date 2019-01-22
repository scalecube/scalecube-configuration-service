package io.scalecube.configuration.benchmarks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.scalecube.account.api.AddOrganizationApiKeyRequest;
import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.CreateOrganizationResponse;
import io.scalecube.account.api.GetOrganizationResponse;
import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Token;
import io.scalecube.benchmarks.BenchmarkSettings;
import io.scalecube.benchmarks.metrics.BenchmarkTimer;
import io.scalecube.benchmarks.metrics.BenchmarkTimer.Context;
import io.scalecube.configuration.api.Acknowledgment;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.CreateRepositoryRequest;
import io.scalecube.configuration.api.FetchRequest;
import io.scalecube.configuration.api.SaveRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public final class ReadConfigValueBenchmark {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReadConfigValueBenchmark.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Starts benchmark.
   *
   * @param args program arguments.
   */
  public static void main(String[] args) {
    BenchmarkSettings settings = BenchmarkSettings.from(args).build();

    int configKeysCount = Integer.parseInt(settings.find("configKeysCount", "100"));
    Token token = new Token("auth0.com", settings.find("token", ""));

    new ConfigurationServiceBenchmarkState(settings)
        .runForAsync(
            state -> {
              OrganizationService organizationService = state.clientFor(OrganizationService.class);
              ConfigurationService configurationService =
                  state.clientFor(ConfigurationService.class);

              AtomicReference<String> apiKey = new AtomicReference<>();

              createOrganization(organizationService, token)
                  .flatMap(organization -> createApiKey(organizationService, token, organization))
                  .flatMap(
                      organization -> {
                        String key = organization.apiKeys()[0].key();
                        apiKey.set(key);
                        return createRepository(configurationService, key);
                      })
                  .flatMapMany(
                      ack ->
                          Flux.range(0, configKeysCount)
                              .flatMap(
                                  keyIndex -> {
                                    String key = "key-" + keyIndex;
                                    JsonNode value = objectMapper.valueToTree(keyIndex);

                                    return saveConfigProperty(
                                        configurationService, apiKey.get(), key, value);
                                  }))
                  .doOnComplete(() -> LOGGER.info("Preloading completed!"))
                  .then()
                  .block();

              BenchmarkTimer timer = state.timer("timer");

              return i -> {
                Context time = timer.time();
                return configurationService
                    .fetch(
                        new FetchRequest(
                            apiKey.get(), "benchmarks-repo", "key-" + i % configKeysCount))
                    .doOnSuccess(response -> time.stop());
              };
            });
  }

  private static Mono<CreateOrganizationResponse> createOrganization(
      OrganizationService organizationService, Token token) {
    return organizationService
        .createOrganization(new CreateOrganizationRequest("benchmarks", token, "info@scalecube.io"))
        .doOnSuccess(response -> LOGGER.info("Organization created: {}", response))
        .doOnError(th -> LOGGER.error("Organization not created: {}", th));
  }

  private static Mono<GetOrganizationResponse> createApiKey(
      OrganizationService organizationService, Token token, OrganizationInfo organization) {
    Map<String, String> claims = new HashMap<>();
    claims.put("role", "Owner");
    return organizationService
        .addOrganizationApiKey(
            new AddOrganizationApiKeyRequest(token, organization.id(), "benchmarksApiKey", claims))
        .doOnSuccess(response -> LOGGER.info("ApiKey created: {}", response))
        .doOnError(th -> LOGGER.error("ApiKey not created: {}", th));
  }

  private static Mono<Acknowledgment> createRepository(
      ConfigurationService configurationService, String apiKey) {
    return configurationService
        .createRepository(new CreateRepositoryRequest(apiKey, "benchmarks-repo"))
        .doOnSuccess(response -> LOGGER.info("Repository created: {}", response))
        .doOnError(th -> LOGGER.error("Repository not created: ", th));
  }

  private static Mono<Acknowledgment> saveConfigProperty(
      ConfigurationService configurationService, String apiKey, String key, JsonNode value) {
    return configurationService
        .save(new SaveRequest(apiKey, "benchmarks-repo", key, value))
        .doOnSuccess(response -> LOGGER.info("Config created: {}={}", key, value))
        .doOnError(th -> LOGGER.error("Config not created: ", th));
  }
}
