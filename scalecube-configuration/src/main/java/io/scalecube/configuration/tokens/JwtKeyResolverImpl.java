package io.scalecube.configuration.tokens;

import io.scalecube.security.jwt.JwtKeyResolver;
import java.security.Key;
import java.time.Duration;
import java.util.Map;

final class JwtKeyResolverImpl implements JwtKeyResolver {

  private static final String KID_HEADER_NAME = "kid";

  private KeyProvider keyProvider;

  JwtKeyResolverImpl(KeyProvider keyProvider) {
    this.keyProvider = keyProvider;
  }

  @Override
  public Key resolve(Map<String, Object> tokenClaims) {
    // TODO: refactor scalecube-security to reactive style
    return keyProvider
        .get(tokenClaims.get(KID_HEADER_NAME).toString())
        .block(Duration.ofSeconds(3));
  }
}
