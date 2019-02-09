package io.scalecube.configuration.api;

public class InvalidAuthenticationToken extends RuntimeException {

  public InvalidAuthenticationToken(String message) {
    super(message);
  }

  public InvalidAuthenticationToken() {}
}
