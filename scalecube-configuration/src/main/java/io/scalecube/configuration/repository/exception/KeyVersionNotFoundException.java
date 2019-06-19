package io.scalecube.configuration.repository.exception;

public class KeyVersionNotFoundException extends DataAccessException {

  public KeyVersionNotFoundException(String message) {
    super(message);
  }

  public KeyVersionNotFoundException(String message, RuntimeException ex) {
    super(message, ex);
  }
}
