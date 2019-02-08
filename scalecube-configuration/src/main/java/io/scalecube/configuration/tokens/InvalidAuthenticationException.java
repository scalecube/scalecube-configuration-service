package io.scalecube.configuration.tokens;

public class InvalidAuthenticationException extends Exception {

  public InvalidAuthenticationException(String message) {
    this(message, null);
  }

  public InvalidAuthenticationException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidAuthenticationException() {}
}
