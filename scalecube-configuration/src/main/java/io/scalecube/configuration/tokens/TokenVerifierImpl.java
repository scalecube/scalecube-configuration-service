package io.scalecube.configuration.tokens;

import io.scalecube.security.JwtAuthenticator;
import io.scalecube.security.JwtAuthenticatorImpl;
import io.scalecube.security.JwtKeyResolver;
import io.scalecube.security.Profile;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class TokenVerifierImpl implements TokenVerifier {

  private static final Logger logger = LoggerFactory.getLogger(TokenVerifierImpl.class);

  private JwtAuthenticator tokenAuthenticator;

  TokenVerifierImpl(KeyProvider keyProvider) {
    JwtKeyResolver keyResolver = new JwtKeyResolverImpl(keyProvider);

    tokenAuthenticator = new JwtAuthenticatorImpl.Builder().keyResolver(keyResolver).build();
  }

  @Override
  public Profile verify(Object token) throws InvalidAuthenticationException {
    Objects.requireNonNull(token, "Token is a required argument");
    try {
      return tokenAuthenticator.authenticate(token.toString());
    } catch (Exception ex) {
      logger.warn("Token verification failed, reason: {}", ex);
      throw new InvalidAuthenticationException("Token verification failed", ex);
    }
  }
}
