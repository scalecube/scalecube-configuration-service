package io.scalecube.configuration.tokens;

import io.scalecube.security.Profile;
import reactor.core.publisher.Mono;

/**
 * Token verification abstraction.
 */
public interface TokenVerifier {

  /**
   * Verifies the token argument and returns a profile based in the given token.
   * @param token the token to verify.
   * @return a profile.
   * @throws InvalidAuthenticationException in case the verification fails.
   */
  Mono<Profile> verify(Object token);
}
