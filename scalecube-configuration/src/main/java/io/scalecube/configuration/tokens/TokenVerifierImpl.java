package io.scalecube.configuration.tokens;

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
      return getTokenAuthenticator().authenticate(token.toString());
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new InvalidAuthenticationException();
    }
  }

  private JwtAuthenticator getTokenAuthenticator() {
    if (authenticator == null) {
      try {
        final Key key = new PublicKeyProviderImpl().get();

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
