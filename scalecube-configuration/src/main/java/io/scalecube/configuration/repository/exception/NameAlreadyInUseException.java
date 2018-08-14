package io.scalecube.configuration.repository.exception;

public class NameAlreadyInUseException extends DataAccessException {

  public NameAlreadyInUseException(String message) {
    super(message, cause);
  }
}
