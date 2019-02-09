package io.scalecube.configuration.tokens;

import io.scalecube.security.DefaultJwtAuthenticator;
import io.scalecube.security.JwtAuthenticator;
import io.scalecube.security.JwtKeyResolver;
import io.scalecube.security.Profile;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

final class TokenVerifierImpl implements TokenVerifier {

  private static final Logger logger = LoggerFactory.getLogger(TokenVerifierImpl.class);

  private JwtAuthenticator jwtAuthenticator;

  TokenVerifierImpl(KeyProvider keyProvider) {
    JwtKeyResolver keyResolver = new JwtKeyResolverImpl(keyProvider);
    jwtAuthenticator = new DefaultJwtAuthenticator(keyResolver);
  }

  @Override
  public Mono<Profile> verify(Object token) {
    return Mono.fromRunnable(() -> Objects.requireNonNull(token, "Token is a required argument"))
        .then(Mono.fromCallable(() -> jwtAuthenticator.authenticate(token.toString())))
        .doOnError(throwable -> logger.warn("Token verification failed, reason: {}", throwable))
        .onErrorMap(th -> new InvalidAuthenticationException("Token verification failed", th));
  }
}
