package io.scalecube.configuration.tokens;

import io.scalecube.security.Profile;

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
  Profile verify(Object token) throws InvalidAuthenticationException;
}
