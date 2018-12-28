package io.scalecube.configuration.tokens;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.response.LogicalResponse;
import io.scalecube.config.ConfigRegistry;
import io.scalecube.configuration.ConfigRegistryConfiguration;
import java.security.Key;
import java.util.Map;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VaultKeyProvider implements KeyProvider {

  private static final String VAULT_ENTRY_KEY = "key";
  private static final int HTTP_STATUS_NOT_FOUND = 404;
  private static final int MAX_RETRIES = 5;
  private static final String VAULT_RETRY_INTERVAL_MILLISECONDS =
      "vault.retry.interval.milliseconds";
  private final int maxRetries;
  private static final int RETRY_INTERVAL_MILLISECONDS = 1000;
  private final int retryIntervalMilliseconds;
  private static final String JWT_ALGORITHM = "jwt.algorithm";
  private static final String DEFAULT_JWT_ALGORITHM = "HmacSHA256";
  private static final String VAULT_MAX_RETRIES_KEY = "vault.max.retries";
  private final String algorithm;
  private static final Logger LOGGER = LoggerFactory.getLogger(VaultKeyProvider.class);
  private final VaultPathBuilder vaultPathBuilder = new VaultPathBuilder();
  private final Vault vault;

  /** Construct an instance of VaultKeyProvider. */
  VaultKeyProvider() {
    try {
      vault = new Vault(new VaultConfig().build());

      ConfigRegistry configRegistry = ConfigRegistryConfiguration.configRegistry();
      algorithm = configRegistry.stringValue(JWT_ALGORITHM, DEFAULT_JWT_ALGORITHM);
      maxRetries = configRegistry.intValue(VAULT_MAX_RETRIES_KEY, MAX_RETRIES);
      retryIntervalMilliseconds =
          configRegistry.intValue(VAULT_RETRY_INTERVAL_MILLISECONDS, RETRY_INTERVAL_MILLISECONDS);
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
      return new SecretKeySpec(DatatypeConverter.parseBase64Binary(vaultEntry), algorithm);
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
    final String path = vaultPathBuilder.getPath(alias);
    final LogicalResponse response;
    Map<String, String> data = null;

    try {
      response = vault.withRetries(maxRetries, retryIntervalMilliseconds).logical().read(path);
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
      final String path = vaultPathBuilder.getPath(alias);
      final String message = String.format("path: '%s' not found", path);
      throw new KeyProviderException(message, ex);
    }

    throw new KeyProviderException(ex);
  }
}
