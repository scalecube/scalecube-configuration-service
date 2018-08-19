package io.scalecube.configuration.api;

public class BadRequest extends Throwable {

  private static final long serialVersionUID = 1L;

  public BadRequest(String message) {

    super(message);
  }

  public BadRequest() {
  }
}



