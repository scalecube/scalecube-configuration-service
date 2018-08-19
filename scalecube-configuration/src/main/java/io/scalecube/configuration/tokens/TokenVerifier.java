package io.scalecube.configuration.tokens;

import io.scalecube.security.Profile;

public interface TokenVerifier {

  Profile verify(Object token) throws InvalidAuthenticationException;
}
