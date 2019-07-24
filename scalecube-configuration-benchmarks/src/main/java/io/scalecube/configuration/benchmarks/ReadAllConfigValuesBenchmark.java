package io.scalecube.configuration.benchmarks;

import io.scalecube.benchmarks.BenchmarkSettings;
import io.scalecube.benchmarks.metrics.BenchmarkTimer;
import io.scalecube.benchmarks.metrics.BenchmarkTimer.Context;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.ReadListRequest;

public final class ReadAllConfigValuesBenchmark {

  /**
   * Starts benchmark.
   *
   * @param args program arguments.
   */
  public static void main(String[] args) {
    BenchmarkSettings settings = BenchmarkSettings.from(args).build();

    new ConfigurationServiceBenchmarkState(settings)
        .runForAsync(
            state -> {
              ConfigurationService configurationService =
                  state.forService(ConfigurationService.class);

              BenchmarkTimer timer = state.timer("timer");

              return i -> {
                ReadListRequest request = new ReadListRequest(state.apiKey(), "benchmarks-repo");

                Context time = timer.time();

                return configurationService.readList(request).doOnSuccess(response -> time.stop());
              };
            });
  }
}
