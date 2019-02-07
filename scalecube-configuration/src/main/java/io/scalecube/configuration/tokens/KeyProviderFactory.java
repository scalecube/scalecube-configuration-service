package io.scalecube.configuration.tokens;

import io.scalecube.config.StringConfigProperty;
import io.scalecube.configuration.AppConfiguration;

final class KeyProviderFactory {

  private static final StringConfigProperty vaultAddr =
      AppConfiguration.configRegistry().stringProperty("VAULT_ADDR");

  static KeyProvider provider;

  static KeyProvider keyProvider() {
    if (provider != null) {
      return provider;
    }
    synchronized (KeyProviderFactory.class) {
      if (provider != null) {
        return provider;
      }
      provider =
          new CachingKeyProvider(
              vaultAddr.value().isPresent() ? new VaultKeyProvider() : new KeyProviderImpl());
    }
    return provider;
  }
}
