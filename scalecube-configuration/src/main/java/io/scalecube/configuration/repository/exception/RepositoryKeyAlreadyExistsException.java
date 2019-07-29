package io.scalecube.configuration.repository.exception;

public class RepositoryKeyAlreadyExistsException extends DataAccessException {

  public RepositoryKeyAlreadyExistsException(String message) {
    super(message, null);
  }
}
