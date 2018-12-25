package io.scalecube.configuration.tokens;

import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.scalecube.security.JwtAuthenticator;
import io.scalecube.security.JwtAuthenticatorImpl;
import io.scalecube.security.Profile;
import java.security.Key;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class TokenVerifierImpl implements TokenVerifier {

  private static final String KID_HEADER_NAME = "kid";
  private final Logger logger = LoggerFactory.getLogger(TokenVerifierImpl.class);

  @Override
  public Profile verify(Object token) throws InvalidAuthenticationException {
    Objects.requireNonNull(token, "Token is a required argument");
    try {
      String keyId = getKeyId(token.toString());
      JwtAuthenticator tokenAuthenticator = getTokenAuthenticator(keyId);
      return tokenAuthenticator.authenticate(token.toString());
    } catch (Exception ex) {
      logger.warn("Token verification failed, reason: {}", ex);
      throw new InvalidAuthenticationException("Token verification failed", ex);
    }
  }

  private String getKeyId(String token) throws InvalidAuthenticationException {
    Header header;

    try {
      String tokenWithoutSignature = token.substring(0, token.lastIndexOf('.') + 1);
      header = Jwts.parser().parseClaimsJwt(tokenWithoutSignature).getHeader();
    } catch (Throwable ex) {
      throw new InvalidAuthenticationException("Failed to acquire token signing key id", ex);
    }

    if (header == null || !header.containsKey(KID_HEADER_NAME)) {
      throw new InvalidAuthenticationException("Missing key id in token header claim");
    }

    return header.get(KID_HEADER_NAME).toString();
  }

  private JwtAuthenticator getTokenAuthenticator(String keyAlias) {
    try {
      Key key = KeyProviderFactory.keyProvider().get(keyAlias);
      // todo propose a good solution for it or just refactor security library
      return new JwtAuthenticatorImpl.Builder().keyResolver(map -> Optional.of(key)).build();
    } catch (Exception ex) {
      throw new TokenVerificationException(ex);
    }
  }
}
