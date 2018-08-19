package io.scalecube.configuration.repository.exception;

public class KeyNotFoundException extends DataAccessException {

  public KeyNotFoundException(String message) {
    super(message);
  }

  public KeyNotFoundException(String message, RuntimeException ex) {
    super(message, ex);
  }
}
