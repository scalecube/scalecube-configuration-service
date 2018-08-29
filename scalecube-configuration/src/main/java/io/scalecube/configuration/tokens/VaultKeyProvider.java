package io.scalecube.configuration.tokens;

import io.scalecube.config.ConfigRegistry;
import io.scalecube.configuration.ConfigRegistryConfiguration;
import java.security.Key;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class VaultKeyProvider implements KeyProvider {

  @Override
  public Key get(String alias) throws KeyProviderException {
    try {
      ConfigRegistry configRegistry = ConfigRegistryConfiguration.configRegistry();
      String secret = configRegistry.stringProperty(alias).valueOrThrow();
      String algorithm = configRegistry
          .stringValue("jwt.algorithm", "HmacSHA256");
      return new SecretKeySpec(DatatypeConverter.parseBase64Binary(secret), algorithm);
    } catch (Exception ex) {
      throw new KeyProviderException(ex);
    }
  }
}
