package io.scalecube.configuration.benchmarks;

import io.scalecube.benchmarks.BenchmarkSettings;
import io.scalecube.benchmarks.metrics.BenchmarkTimer;
import io.scalecube.benchmarks.metrics.BenchmarkTimer.Context;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.ReadEntryRequest;

public final class ReadConfigValueBenchmark {

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
                ReadEntryRequest readEntryRequest =
                    new ReadEntryRequest(
                        state.apiKey(), "benchmarks-repo", "key-" + i % configKeysCount);

                Context time = timer.time();

                return configurationService
                    .readEntry(readEntryRequest)
                    .doOnSuccess(response -> time.stop());
              };
            });
  }
}
