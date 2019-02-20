package io.scalecube.configuration.repository.exception;

public class RepositoryAlreadyExistsException extends DataAccessException {

  public RepositoryAlreadyExistsException(String message) {
    super(message, null);
  }
}
