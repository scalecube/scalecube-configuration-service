package io.scalecube.configuration.tokens;

import io.scalecube.security.JwtKeyResolver;
import java.security.Key;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class JwtKeyResolverImpl implements JwtKeyResolver {

  private static final Logger LOGGER = LoggerFactory.getLogger(JwtKeyResolverImpl.class);

  private static final String KID_HEADER_NAME = "kid";

  private KeyProvider keyProvider;

  JwtKeyResolverImpl(KeyProvider keyProvider) {
    this.keyProvider = keyProvider;
  }

  @Override
  public Optional<Key> resolve(Map<String, Object> tokenClaims) {
    return Optional.ofNullable(tokenClaims.get(KID_HEADER_NAME).toString())
        .map(
            keyId -> {
              try {
                return keyProvider.get(keyId);
              } catch (KeyProviderException e) {
                LOGGER.warn("Error resolving key", e);
                return null;
              }
            });
  }
}
