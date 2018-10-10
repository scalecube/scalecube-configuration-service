package io.scalecube.configuration.tokens;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
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
  public static final int HTTP_STATUS_NOT_FOUND = 404;
  private final String algorithm;
  private static final Logger LOGGER = LoggerFactory.getLogger(VaultKeyProvider.class);
  private final VaultPathBuilder vaultPathBuilder = new VaultPathBuilder();

  /**
   * Construct an instance of VaultKeyProvider.
   */
  VaultKeyProvider() {
    try {
      ConfigRegistry configRegistry = ConfigRegistryConfiguration.configRegistry();
      algorithm = configRegistry.stringValue("jwt.algorithm", "HmacSHA256");
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public Key get(String alias) throws KeyProviderException {
    return getSecretKey(alias);
  }

  private Key getSecretKey(String alias)  throws KeyProviderException {
    try {
      String vaultEntry = getVaultEntryValue(alias);
      return new SecretKeySpec(
          DatatypeConverter.parseBase64Binary(vaultEntry), algorithm);
    } catch (Exception ex) {
      LOGGER.error(String.format("Error creating key for alias: '%s'", alias), ex);
      if (ex instanceof KeyProviderException) {
        throw (KeyProviderException)ex;
      } else if (ex instanceof VaultException) {
        handleVaultException((VaultException)ex, alias);
      } else {
        throw new KeyProviderException(ex);
      }
    }
    return null;
  }

  private String getVaultEntryValue(String alias) throws VaultException, KeyProviderException {
    final Vault vault = new Vault(new VaultConfig().build());
    final String path = vaultPathBuilder.getPath(alias);
    final Map<String, String> data = vault.logical().read(path).getData();

    if (data == null || data.isEmpty() || !data.containsKey(VAULT_ENTRY_KEY)) {
      throw new KeyProviderException(String.format("'%s' was expected under secret '%s'",
          VAULT_ENTRY_KEY, path));
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
