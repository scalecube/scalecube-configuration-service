package io.scalecube.configuration.tokens;

import io.scalecube.config.StringConfigProperty;
import io.scalecube.configuration.AppConfiguration;

public final class KeyProviderFactory {

  private static final StringConfigProperty vaultAddr =
      AppConfiguration.configRegistry().stringProperty("VAULT_ADDR");

  private KeyProviderFactory() {}

  public static KeyProvider keyProvider() {
    return vaultAddr.value().isPresent() ? new VaultKeyProvider() : new KeyProviderImpl();
  }
}
