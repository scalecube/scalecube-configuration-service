package io.scalecube.server;

import io.scalecube.configuration.AppSettings;
import io.scalecube.configuration.ConfigurationServiceImpl;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.repository.DataAccessFactory;
import io.scalecube.configuration.tokens.TokenVerifierFactory;
import io.scalecube.services.Microservices;
import io.scalecube.transport.Address;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ConfigurationServiceRunner {

  private static final List<String> DEFAULT_SEEDS = Collections.singletonList("seed:4802");
  private static final String SEEDS = "seeds";

  /**
   * Application main entry.
   *
   * @param args application params.
   */
  public static void main(String[] args) throws Exception {
    start();
    Thread.currentThread().join();
  }

  private static void start() throws Exception {
    Microservices.builder()
        .seeds(seeds())
        .services(createConfigurationService())
        .startAwait();
  }

  private static ConfigurationService createConfigurationService() {
    return ConfigurationServiceImpl.builder()
        .dataAccess(DataAccessFactory.dataAccess())
        .tokenVerifier(TokenVerifierFactory.tokenVerifier())
        .build();
  }

  private static Address[] seeds() throws Exception {
    AppSettings settings = AppSettings.builder().build();
    try {
      return stringListValue(settings.getProperty(SEEDS))
          .stream().map(Address::from).toArray(Address[]::new);
    } catch (Throwable ex) {
      throw new Exception("Failed to parse seeds from settings", ex);
    }
  }

  private static List<String> stringListValue(String seeds) {
    if (seeds == null || seeds.length() == 0) {
      return DEFAULT_SEEDS;
    } else {
      return Arrays.asList(seeds.split(","));
    }
  }
}

