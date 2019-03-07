package io.scalecube.configuration.benchmarks;

import io.scalecube.benchmarks.BenchmarkSettings;
import io.scalecube.benchmarks.metrics.BenchmarkTimer;
import io.scalecube.benchmarks.metrics.BenchmarkTimer.Context;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.FetchRequest;

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
                  state.client().forService(ConfigurationService.class);

              BenchmarkTimer timer = state.timer("timer");

              return i -> {
                FetchRequest fetchRequest =
                    new FetchRequest(
                        state.apiKey(), "benchmarks-repo", "key-" + i % configKeysCount);

                Context time = timer.time();

                return configurationService
                    .fetch(fetchRequest)
                    .doOnSuccess(response -> time.stop());
              };
            });
  }
}
