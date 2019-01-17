package io.scalecube.configuration.tokens;

import io.scalecube.config.ConfigRegistry;
import io.scalecube.config.StringConfigProperty;
import io.scalecube.configuration.AppConfiguration;

final class VaultPathBuilder {

  private StringConfigProperty vaultSecretsPath;
  private StringConfigProperty apiKeysPathPattern;

  VaultPathBuilder() {
    ConfigRegistry configRegistry = AppConfiguration.configRegistry();

    vaultSecretsPath = configRegistry.stringProperty("VAULT_SECRETS_PATH");
    apiKeysPathPattern = configRegistry.stringProperty("api.keys.path.pattern");
  }

  String getPath(String alias) {
    return String.format(apiKeysPathPattern.valueOrThrow(), vaultSecretsPath.valueOrThrow())
        .concat(alias);
  }
}
