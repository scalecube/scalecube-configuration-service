package io.scalecube.configuration.benchmarks;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.scalecube.benchmarks.BenchmarkSettings;
import io.scalecube.benchmarks.metrics.BenchmarkTimer;
import io.scalecube.benchmarks.metrics.BenchmarkTimer.Context;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.CreateOrUpdateEntryRequest;

public final class UpdateConfigValueBenchmark {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  /**
   * Starts benchmark.
   *
   * @param args program arguments.
   */
  public static void main(String[] args) {
    BenchmarkSettings settings = BenchmarkSettings.from(args).build();

    int configKeysCount = Integer.parseInt(settings.find("configKeysCount", "100"));

    new ConfigurationServiceBenchmarkState(settings)
        .runForAsync(
            state -> {
              ConfigurationService configurationService =
                  state.forService(ConfigurationService.class);

              BenchmarkTimer timer = state.timer("timer");

              return i -> {
                CreateOrUpdateEntryRequest createOrUpdateEntryRequest =
                    new CreateOrUpdateEntryRequest(
                        state.apiKey(),
                        "benchmarks-repo",
                        "key-" + i % configKeysCount,
                        OBJECT_MAPPER.valueToTree(i + 1));

                Context time = timer.time();

                return configurationService
                    .createEntry(createOrUpdateEntryRequest)
                    .doOnSuccess(response -> time.stop());
              };
            });
  }
}
