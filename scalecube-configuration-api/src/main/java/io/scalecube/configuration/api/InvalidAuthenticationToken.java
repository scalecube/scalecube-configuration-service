package io.scalecube.configuration.api;

public class InvalidAuthenticationToken extends Throwable {

  private static final long serialVersionUID = 1L;

  public InvalidAuthenticationToken(String message) {

    super(message);
  }

  public InvalidAuthenticationToken() {
  }
}
