package io.scalecube.configuration.tokens;

public abstract class TokenVerifierFactory {
  public static TokenVerifier tokenVerifier() {
    return new TokenVerifierImpl();
  }
}
