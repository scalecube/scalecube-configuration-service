package io.scalecube.server;

import com.couchbase.client.java.AsyncCluster;
import com.couchbase.client.java.CouchbaseAsyncCluster;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.app.decoration.Logo;
import io.scalecube.app.packages.PackageInfo;
import io.scalecube.config.ConfigRegistry;
import io.scalecube.configuration.AppConfiguration;
import io.scalecube.configuration.ConfigurationServiceImpl;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.authorization.DefaultPermissions;
import io.scalecube.configuration.repository.ConfigurationRepository;
import io.scalecube.configuration.repository.couchbase.CouchbaseAdmin;
import io.scalecube.configuration.repository.couchbase.CouchbaseRepository;
import io.scalecube.configuration.repository.couchbase.CouchbaseSettings;
import io.scalecube.configuration.tokens.OrganizationServiceKeyProvider;
import io.scalecube.security.acl.DefaultAccessControl;
import io.scalecube.security.api.AccessControl;
import io.scalecube.security.api.Authenticator;
import io.scalecube.security.jwt.DefaultJwtAuthenticator;
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
    CouchbaseSettings settings =
        configRegistry.objectProperty("couchbase", CouchbaseSettings.class).value(null);

    CouchbaseEnvironment env = DefaultCouchbaseEnvironment.create();

    CouchbaseAdmin couchbaseAdmin =
        new CouchbaseAdmin(settings, couchbaseAdminCluster(settings, env));

    ConfigurationRepository configurationRepository =
        new CouchbaseRepository(
            settings, couchbaseDataAccessCluster(settings, env), couchbaseAdmin);

    return call -> {
      OrganizationService organizationService = call.create().api(OrganizationService.class);

      OrganizationServiceKeyProvider keyProvider =
          new OrganizationServiceKeyProvider(organizationService);

      Authenticator authenticator =
          new DefaultJwtAuthenticator(
              map -> keyProvider.get(map.get("kid").toString()).block());

      AccessControl accessContorl =
          DefaultAccessControl.builder()
              .authenticator(authenticator)
              .authorizer(DefaultPermissions.PERMISSIONS)
              .build();

      ConfigurationService configurationService =
          new ConfigurationServiceImpl(configurationRepository, accessContorl);

      return Collections.singleton(ServiceInfo.fromServiceInstance(configurationService).build());
    };
  }

  private static AsyncCluster couchbaseDataAccessCluster(
      CouchbaseSettings settings, CouchbaseEnvironment env) {
    return CouchbaseAsyncCluster.create(env, settings.hosts());
  }

  private static AsyncCluster couchbaseAdminCluster(
      CouchbaseSettings settings, CouchbaseEnvironment env) {
    List<String> nodes = settings.hosts();
    AsyncCluster cluster =
        nodes.isEmpty()
            ? CouchbaseAsyncCluster.create(env)
            : CouchbaseAsyncCluster.create(env, nodes);
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
