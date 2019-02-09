package io.scalecube.configuration.api;

public class BadRequest extends RuntimeException {

  public BadRequest(String message) {
    super(message);
  }

  public BadRequest() {}
}
