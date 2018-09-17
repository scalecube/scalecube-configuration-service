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
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationServiceRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationServiceRunner.class);

  /**
   * Application main entry.
   *
   * @param args application params.
   */
  public static void main(String[] args) throws Exception {
    start();
    Thread.currentThread().join();
  }

  private static void start() {
    DiscoveryOptions discoveryOptions = discoveryOptions();
    LOGGER.info("Starting configuration service on {}", discoveryOptions);

    Microservices.builder()
        .discovery(
            options ->
                options
                    .seeds(discoveryOptions.seeds())
                    .port(discoveryOptions.discoveryPort())
                    .memberHost(discoveryOptions.memberHost())
                    .memberPort(discoveryOptions.memberPort()))
        .servicePort(discoveryOptions.servicePort())
        .services(createConfigurationService())
        .startAwait();
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

  private static Cluster couchbaseDataAccessCluster(CouchbaseSettings settings) {
    return CouchbaseCluster.create(settings.hosts());
  }

  private static Cluster couchbaseAdminCluster(CouchbaseSettings settings) {
    List<String> nodes = settings.hosts();
    Cluster cluster = nodes.isEmpty() ? CouchbaseCluster.create() : CouchbaseCluster.create(nodes);
    cluster.authenticate(settings.username(), settings.password());
    return cluster;
  }

  private static DiscoveryOptions discoveryOptions() {
    ConfigRegistry configRegistry = ConfigRegistryConfiguration.configRegistry();
    return configRegistry
        .objectProperty("io.scalecube.configuration", DiscoveryOptions.class)
        .value()
        .orElseThrow(() -> new IllegalStateException("Couldn't load discovery options"));
  }

  public static class DiscoveryOptions {

    private List<String> seeds;
    private Integer servicePort;
    private Integer discoveryPort;
    private String memberHost;
    private Integer memberPort;

    public int servicePort() {
      return servicePort != null ? servicePort : 0;
    }

    public Integer discoveryPort() {
      return discoveryPort;
    }

    /**
     * Returns seeds as an {@link Address}'s array.
     *
     * @return {@link Address}'s array
     */
    public Address[] seeds() {
      return Optional.ofNullable(seeds)
          .map(seeds -> seeds.stream().map(Address::from).toArray(Address[]::new))
          .orElse(new Address[0]);
    }

    public String memberHost() {
      return memberHost;
    }

    public Integer memberPort() {
      return memberPort;
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder("DiscoveryOptions{");
      sb.append("seeds=").append(seeds);
      sb.append(", servicePort=").append(servicePort);
      sb.append(", discoveryPort=").append(discoveryPort);
      sb.append(", memberHost=").append(memberHost);
      sb.append(", memberPort=").append(memberPort);
      sb.append('}');
      return sb.toString();
    }
  }
}
