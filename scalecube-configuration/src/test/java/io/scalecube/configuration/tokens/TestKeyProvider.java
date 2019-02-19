package io.scalecube.configuration.tokens;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.Key;
import java.util.Base64;
import java.util.Properties;
import javax.crypto.spec.SecretKeySpec;
import reactor.core.publisher.Mono;

public class TestKeyProvider implements KeyProvider {

  private static final String HMAC_SHA_256 = "HmacSHA256";

  @Override
  public Mono<Key> get(String keyId) {
    return Mono.fromCallable(() -> getKey(keyId));
  }

  private Key getKey(String alias) {
    Properties properties = new Properties();
    try {
      if (new File("keystore.properties").exists()) {
        properties.load(new FileInputStream("keystore.properties"));
      }
    } catch (IOException ex) {
      throw new KeyProviderException(ex);
    }

    if (!properties.containsKey(alias)) {
      throw new KeyProviderException(String.format("Key with alias: '%s' not found", alias));
    }
    // decode the base64 encoded string
    byte[] decodedKey = Base64.getDecoder().decode(properties.get(alias).toString().getBytes());
    // rebuild key using SecretKeySpec
    return new SecretKeySpec(decodedKey, 0, decodedKey.length, HMAC_SHA_256);
  }
}