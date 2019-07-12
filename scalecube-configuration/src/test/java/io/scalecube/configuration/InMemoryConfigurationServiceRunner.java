package io.scalecube.configuration;

import io.scalecube.account.api.OrganizationService;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.authorization.DefaultPermissions;
import io.scalecube.configuration.repository.InMemoryConfigurationRepository;
import io.scalecube.configuration.tokens.OrganizationServiceKeyProvider;
import io.scalecube.net.Address;
import io.scalecube.security.acl.DefaultAccessControl;
import io.scalecube.security.api.AccessControl;
import io.scalecube.security.api.Authenticator;
import io.scalecube.security.jwt.DefaultJwtAuthenticator;
import io.scalecube.services.Microservices;
import io.scalecube.services.ServiceInfo;
import io.scalecube.services.ServiceProvider;
import io.scalecube.services.discovery.ScalecubeServiceDiscovery;
import io.scalecube.services.transport.rsocket.RSocketServiceTransport;
import java.util.Collections;

public class InMemoryConfigurationServiceRunner {

  /**
   * Application main entry.
   *
   * @param args application params.
   */
  public static void main(String[] args) {
    Microservices.builder()
        .discovery(
            (serviceEndpoint) ->
                new ScalecubeServiceDiscovery(serviceEndpoint)
                    .options(
                        opts ->
                            opts.membership(
                                cfg -> cfg.seedMembers(Address.from("localhost:4801")))))
        .transport(RSocketServiceTransport::new)
        .services(createConfigurationService())
        .startAwait()
        .onShutdown()
        .block();
  }

  private static ServiceProvider createConfigurationService() {
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

      ConfigurationService service =
          new ConfigurationServiceImpl(new InMemoryConfigurationRepository(), accessControl);

      return Collections.singleton(ServiceInfo.fromServiceInstance(service).build());
    };
  }
}
