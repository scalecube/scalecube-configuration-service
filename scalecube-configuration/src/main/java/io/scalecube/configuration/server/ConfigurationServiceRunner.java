package io.scalecube.configuration.server;

import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.CouchbaseCluster;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.app.decoration.Logo;
import io.scalecube.app.packages.PackageInfo;
import io.scalecube.config.ConfigRegistry;
import io.scalecube.configuration.AppConfiguration;
import io.scalecube.configuration.ConfigurationServiceImpl;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.authorization.DefaultPermissions;
import io.scalecube.configuration.repository.ConfigurationRepository;
import io.scalecube.configuration.repository.couchbase.CouchbaseRepository;
import io.scalecube.configuration.repository.couchbase.CouchbaseSettings;
import io.scalecube.configuration.tokens.OrganizationServiceKeyProvider;
import io.scalecube.security.acl.DefaultAccessControl;
import io.scalecube.security.api.AccessControl;
import io.scalecube.security.api.Authenticator;
import io.scalecube.security.jwt.DefaultJwtAuthenticator;
import io.scalecube.services.Microservices;
import io.scalecube.services.ServiceEndpoint;
import io.scalecube.services.ServiceInfo;
import io.scalecube.services.ServiceProvider;
import io.scalecube.services.discovery.ScalecubeServiceDiscovery;
import io.scalecube.services.transport.rsocket.RSocketServiceTransport;
import java.time.Duration;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.tcp.TcpClient;
import reactor.netty.tcp.TcpServer;

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
        .discovery((serviceEndpoint) -> serviceDiscovery(discoveryOptions, serviceEndpoint))
        .transport(() -> serviceTransport(discoveryOptions))
        .services(createConfigurationService())
        .start()
        .doOnNext(
            microservices ->
                Logo.from(new PackageInfo())
                    .ip(microservices.discovery().address().host())
                    .port("" + microservices.discovery().address().port())
                    .draw())
        .block()
        .onShutdown()
        .block();
  }

  private static RSocketServiceTransport serviceTransport(DiscoveryOptions options) {
    return new RSocketServiceTransport()
        .tcpClient(resources -> TcpClient.newConnection().runOn(resources))
        .tcpServer(resources -> TcpServer.create().port(options.servicePort()).runOn(resources));
  }

  private static ScalecubeServiceDiscovery serviceDiscovery(
      DiscoveryOptions discoveryOptions, ServiceEndpoint serviceEndpoint) {
    return new ScalecubeServiceDiscovery(serviceEndpoint)
        .options(
            opts ->
                opts.membership(m -> m.seedMembers(discoveryOptions.seeds()))
                    .transport(t -> t.port(discoveryOptions.discoveryPort()))
                    .memberHost(discoveryOptions.memberHost())
                    .memberPort(discoveryOptions.memberPort()));
  }

  private static ServiceProvider createConfigurationService() {
    ConfigRegistry configRegistry = AppConfiguration.configRegistry();

    CouchbaseSettings settings =
        configRegistry.objectProperty("couchbase", CouchbaseSettings.class).value(null);

    ConfigurationRepository configurationRepository =
        new CouchbaseRepository(couchbaseBucket(settings));

    return call -> {
      OrganizationService organizationService = call.api(OrganizationService.class);

      OrganizationServiceKeyProvider keyProvider =
          new OrganizationServiceKeyProvider(organizationService);

      Authenticator authenticator =
          new DefaultJwtAuthenticator(map -> keyProvider.get(map.get("kid").toString()));

      AccessControl accessControl =
          DefaultAccessControl.builder()
              .authenticator(authenticator)
              .authorizer(DefaultPermissions.PERMISSIONS)
              .build();

      ConfigurationService configurationService =
          new ConfigurationServiceImpl(configurationRepository, accessControl);

      return Collections.singleton(ServiceInfo.fromServiceInstance(configurationService).build());
    };
  }

  private static AsyncBucket couchbaseBucket(CouchbaseSettings settings) {
    return Mono.fromCallable(() -> newAsyncBucket(settings))
        .retryBackoff(3, Duration.ofSeconds(1))
        .block(Duration.ofSeconds(30));
  }

  private static AsyncBucket newAsyncBucket(CouchbaseSettings settings) {
    return CouchbaseCluster.create(settings.hosts())
        .authenticate(settings.username(), settings.password())
        .openBucket(settings.bucketName())
        .async();
  }

  private static DiscoveryOptions discoveryOptions() {
    ConfigRegistry configRegistry = AppConfiguration.configRegistry();
    return configRegistry
        .objectProperty("io.scalecube.configuration", DiscoveryOptions.class)
        .value()
        .orElseThrow(() -> new IllegalStateException("Couldn't load discovery options"));
  }
}
