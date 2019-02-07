package io.scalecube.server;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.app.decoration.Logo;
import io.scalecube.app.packages.PackageInfo;
import io.scalecube.config.ConfigRegistry;
import io.scalecube.configuration.AppConfiguration;
import io.scalecube.configuration.ConfigurationServiceImpl;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.repository.ConfigurationDataAccess;
import io.scalecube.configuration.repository.couchbase.CouchbaseAdmin;
import io.scalecube.configuration.repository.couchbase.CouchbaseDataAccess;
import io.scalecube.configuration.repository.couchbase.CouchbaseSettings;
import io.scalecube.configuration.tokens.KeyProviderFactory;
import io.scalecube.configuration.tokens.TokenVerifierFactory;
import io.scalecube.services.Microservices;
import io.scalecube.services.ServiceInfo;
import io.scalecube.services.ServiceProvider;
import java.util.Collections;
import java.util.List;
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

    Microservices microservices =
        Microservices.builder()
            .discovery(
                options ->
                    options
                        .seeds(discoveryOptions.seeds())
                        .port(discoveryOptions.discoveryPort())
                        .memberHost(discoveryOptions.memberHost())
                        .memberPort(discoveryOptions.memberPort()))
            .transport(options -> options.port(discoveryOptions.servicePort()))
            .services(createConfigurationService())
            .startAwait();
    Logo.from(new PackageInfo())
        .ip(microservices.serviceAddress().getHostName())
        .port(microservices.serviceAddress().getPort() + "")
        .draw();
  }

  private static ServiceProvider createConfigurationService() {
    ConfigRegistry configRegistry = AppConfiguration.configRegistry();
    CouchbaseSettings settings = //
        configRegistry.objectProperty("couchbase", CouchbaseSettings.class).value(null);

    CouchbaseAdmin couchbaseAdmin = //
        new CouchbaseAdmin(settings, couchbaseAdminCluster(settings));

    ConfigurationDataAccess configurationDataAccess = //
        new CouchbaseDataAccess(settings, couchbaseDataAccessCluster(settings), couchbaseAdmin);

    return call -> {
      OrganizationService organizationService = call.create().api(OrganizationService.class);
      
      KeyProviderFactory.withOrganizationService(organizationService);
      ConfigurationService configurationService = ConfigurationServiceImpl.builder()
          .dataAccess(configurationDataAccess)
          .tokenVerifier(TokenVerifierFactory.tokenVerifier())
          .build();
      
      return Collections.singleton(ServiceInfo.fromServiceInstance(configurationService).build()); 
    };
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
    ConfigRegistry configRegistry = AppConfiguration.configRegistry();
    return configRegistry
        .objectProperty("io.scalecube.configuration", DiscoveryOptions.class)
        .value()
        .orElseThrow(() -> new IllegalStateException("Couldn't load discovery options"));
  }
}
