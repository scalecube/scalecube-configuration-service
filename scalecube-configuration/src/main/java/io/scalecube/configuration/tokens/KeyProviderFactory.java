package io.scalecube.configuration.tokens;

abstract class KeyProviderFactory {
  static KeyProvider keyProvider() {
    return System.getenv("VAULT_ADDR") != null ? new VaultKeyProvider() : new KeyProviderImpl();
  }
}
