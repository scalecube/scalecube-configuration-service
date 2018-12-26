package io.scalecube.configuration.tokens;

public class TokenVerificationException extends RuntimeException   {

  public TokenVerificationException(String message, Exception cause) {
    super(message, cause);
  }

  public TokenVerificationException(Exception cause) {
    super(cause);
  }
}
