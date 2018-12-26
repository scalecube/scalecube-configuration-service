package io.scalecube.configuration.tokens;

import com.bettercloud.vault.EnvironmentLoader;
import io.scalecube.configuration.AppSettings;
import java.util.Objects;

final class VaultPathBuilder {

  private String vaultSecretPath;

  String getPath(String alias) {
    return getVaultSecretPath().concat(alias);
  }

  private String getVaultSecretPath() {
    if (vaultSecretPath == null) {
      vaultSecretPath = String.format(
          getVaultSecretPathTokensKeyPattern(), getVaultKeyValueEngine());
    }
    return vaultSecretPath;
  }

  private String getVaultSecretPathTokensKeyPattern() {
    String vaultSecretPathKeyPattern = AppSettings
        .builder()
        .build()
        .getProperty("vault.secret.path");
    Objects.requireNonNull(vaultSecretPathKeyPattern,
        "missing 'vault.secret.path' in settings file");
    return vaultSecretPathKeyPattern;
  }

  private String getVaultKeyValueEngine() {
    EnvironmentLoader environmentLoader = new EnvironmentLoader();
    String vaultKeyValueEngine = environmentLoader.loadVariable("VAULT_SECRETS_PATH");
    Objects.requireNonNull(vaultKeyValueEngine,
        "missing 'VAULT_SECRETS_PATH' env variable");
    return vaultKeyValueEngine;
  }
}
