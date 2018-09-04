package io.scalecube.server;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import io.scalecube.config.ConfigRegistry;
import io.scalecube.configuration.ConfigRegistryConfiguration;
import io.scalecube.configuration.ConfigurationServiceImpl;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.repository.ConfigurationDataAccess;
import io.scalecube.configuration.repository.couchbase.CouchbaseAdmin;
import io.scalecube.configuration.repository.couchbase.CouchbaseDataAccess;
import io.scalecube.configuration.repository.couchbase.CouchbaseSettings;
import io.scalecube.configuration.tokens.TokenVerifierFactory;
import io.scalecube.services.Microservices;
import io.scalecube.transport.Address;
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

    Microservices.builder().seeds(seeds()).services(createConfigurationService()).startAwait();
  }

  private static ConfigurationService createConfigurationService() {
    ConfigRegistry configRegistry = ConfigRegistryConfiguration.configRegistry();
    CouchbaseSettings settings = //
        configRegistry.objectProperty("couchbase", CouchbaseSettings.class).value(null);

    CouchbaseAdmin couchbaseAdmin = //
        new CouchbaseAdmin(settings, couchbaseAdminCluster(settings));

    ConfigurationDataAccess configurationDataAccess = //
        new CouchbaseDataAccess(settings, couchbaseDataAccessCluster(settings), couchbaseAdmin);

    return ConfigurationServiceImpl.builder()
        .dataAccess(configurationDataAccess)
        .tokenVerifier(TokenVerifierFactory.tokenVerifier())
        .build();
  }

  private static Address[] seeds() throws Exception {
    ConfigRegistry configRegistry = ConfigRegistryConfiguration.configRegistry();
    try {
      return configRegistry
          .stringListValue(SEEDS, DEFAULT_SEEDS)
          .stream()
          .map(Address::from)
          .toArray(Address[]::new);
    } catch (Throwable ex) {
      throw new Exception("Failed to parse seeds from settings", ex);
    }
  }

  private static Cluster couchbaseDataAccessCluster(CouchbaseSettings settings) {
    return CouchbaseCluster.create(settings.hosts());
  }

  private static Cluster couchbaseAdminCluster(CouchbaseSettings settings) {
    List<String> nodes = settings.hosts();
    Cluster cluster = nodes.isEmpty() ? CouchbaseCluster.create() : CouchbaseCluster.create(nodes);
    cluster.authenticate(settings.username(), settings.password());
    return cluster;
  }
}
