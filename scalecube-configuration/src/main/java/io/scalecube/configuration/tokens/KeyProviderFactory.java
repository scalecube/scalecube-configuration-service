package io.scalecube.configuration.tokens;

import io.scalecube.account.api.OrganizationService;
import io.scalecube.config.StringConfigProperty;
import io.scalecube.configuration.AppConfiguration;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class is a Factory for {@link KeyProvider key providers}. Generally, once set in an
 * environment - the same provider will be returned.
 */
public final class KeyProviderFactory {

  private static final AtomicReference<OrganizationService> organizationService =
      new AtomicReference<>();

  static KeyProvider provider;

  static KeyProvider keyProvider() {
    if (provider != null) {
      return provider;
    }
    synchronized (KeyProviderFactory.class) {
      if (provider != null) {
        return provider;
      }
      OrganizationService service = organizationService.get();
      if (service != null) {
        provider = new CachingKeyProvider(new OrganizationServiceKeyProvider(service));
      } else {
        provider =
            new CachingKeyProvider(new KeyProviderImpl());
      }
    }
    return provider;
  }

  /**
   * Make the Key Provider Factory work with an {@link OrganizationService organization service}.
   *
   * @param service the service to get public keys from
   * @see OrganizationService#getPublicKey(String)
   */
  public static void withOrganizationService(OrganizationService service) {
    organizationService.set(service);
  }
}
