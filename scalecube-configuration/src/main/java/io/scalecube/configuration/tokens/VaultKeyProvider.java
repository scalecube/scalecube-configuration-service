package io.scalecube.configuration.tokens;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.response.LogicalResponse;
import io.scalecube.config.ConfigRegistry;
import io.scalecube.config.IntConfigProperty;
import io.scalecube.config.StringConfigProperty;
import io.scalecube.configuration.AppConfiguration;
import java.security.Key;
import java.util.Map;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VaultKeyProvider implements KeyProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(VaultKeyProvider.class);

  private static final String DEFAULT_JWT_ALGORITHM = "HmacSHA256";
  private static final int DEFAULT_RETRY_INTERVAL = 1000;
  private static final int DEFAULT_MAX_RETRIES = 5;

  private static final ConfigRegistry configRegistry = AppConfiguration.configRegistry();

  private static final StringConfigProperty jwtAlgorithm =
      configRegistry.stringProperty("jwt.algorithm");
  private static final IntConfigProperty maxRetries =
      configRegistry.intProperty("vault.retry.interval.milliseconds");
  private static final IntConfigProperty retryInterval =
      configRegistry.intProperty("vault.max.retries");

  private static final StringConfigProperty vaultAddr = configRegistry.stringProperty("VAULT_ADDR");
  private static final StringConfigProperty vaultToken =
      configRegistry.stringProperty("VAULT_TOKEN");
  private static final StringConfigProperty vaultSecretsPath =
      configRegistry.stringProperty("VAULT_SECRETS_PATH");

  private static final StringConfigProperty apiKeysPathPattern =
      configRegistry.stringProperty("api.keys.path.pattern");

  private static final String VAULT_ENTRY_KEY = "key";
  private static final int HTTP_STATUS_NOT_FOUND = 404;

  private final Vault vault;

  /** Construct an instance of VaultKeyProvider. */
  VaultKeyProvider() {
    try {
      VaultConfig vaultConfig =
          new VaultConfig()
              .address(vaultAddr.valueOrThrow())
              .token(vaultToken.valueOrThrow())
              .build();
      vault = new Vault(vaultConfig);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public Key get(String alias) throws KeyProviderException {
    return getSecretKey(alias);
  }

  private Key getSecretKey(String alias) throws KeyProviderException {
    try {
      String vaultEntry = getVaultEntryValue(alias);
      return new SecretKeySpec(
          DatatypeConverter.parseBase64Binary(vaultEntry),
          jwtAlgorithm.value().orElse(DEFAULT_JWT_ALGORITHM));
    } catch (Exception ex) {
      LOGGER.error(String.format("Error creating key for alias: '%s'", alias), ex);
      if (ex instanceof KeyProviderException) {
        throw (KeyProviderException) ex;
      } else {
        throw new KeyProviderException(ex);
      }
    }
  }

  private String getVaultEntryValue(String alias) throws KeyProviderException {
    final String path = getPath(alias);
    final LogicalResponse response;
    Map<String, String> data = null;

    try {
      response =
          vault
              .withRetries(
                  maxRetries.value().orElse(DEFAULT_MAX_RETRIES),
                  retryInterval.value().orElse(DEFAULT_RETRY_INTERVAL))
              .logical()
              .read(path);
      data = response.getData();
    } catch (VaultException ex) {
      handleVaultException(ex, alias);
    }

    if (data == null || data.isEmpty() || !data.containsKey(VAULT_ENTRY_KEY)) {
      throw new KeyProviderException(
          String.format("'%s' was expected under secret '%s'", VAULT_ENTRY_KEY, path));
    }

    return data.get(VAULT_ENTRY_KEY);
  }

  private void handleVaultException(VaultException ex, String alias) throws KeyProviderException {
    if (ex.getHttpStatusCode() == HTTP_STATUS_NOT_FOUND) {
      final String path = getPath(alias);
      final String message = String.format("path: '%s' not found", path);
      throw new KeyProviderException(message, ex);
    }

    throw new KeyProviderException(ex);
  }

  private String getPath(String alias) {
    return String.format(apiKeysPathPattern.valueOrThrow(), vaultSecretsPath.valueOrThrow())
        .concat(alias);
  }
}
