package io.scalecube.configuration.tokens;

public final class TokenVerifierFactory {

  public static TokenVerifier tokenVerifier(KeyProvider keyProvider) {
    return new TokenVerifierImpl(keyProvider);
  }
}
