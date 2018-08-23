package io.scalecube.configuration.tokens;

import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.scalecube.security.JwtAuthenticator;
import io.scalecube.security.JwtAuthenticatorImpl;
import io.scalecube.security.Profile;

import java.security.Key;
import java.util.Objects;
import java.util.Optional;

final class TokenVerifierImpl implements TokenVerifier {
  private JwtAuthenticator authenticator;

  @Override
  public Profile verify(Object token) throws InvalidAuthenticationException {
    Objects.requireNonNull(token, "Token is a required argument");
    try {
      return getTokenAuthenticator(getKeyId(token.toString())).authenticate(token.toString());
    } catch (Exception ex) {
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

    if (header == null || !header.containsKey("kid")) {
      throw new InvalidAuthenticationException("Missing key id in token header claim");
    }

    return header.get("kid").toString();
  }

  private JwtAuthenticator getTokenAuthenticator(String keyAlias) {
    if (authenticator == null) {
      try {
        Key key = new KeyProviderImpl().get(keyAlias);

        authenticator = new JwtAuthenticatorImpl
            .Builder()
            .keyResolver(map -> Optional.of(key))
            .build();
      } catch (Exception ex) {
        throw new TokenVerificationException(ex);
      }
    }
    return authenticator;
  }
}
