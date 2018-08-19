package io.scalecube.configuration.repository.exception;

public class InvalidRepositoryNameException extends DataAccessException {

  public InvalidRepositoryNameException(String message) {
    super(message, null);
  }
}
