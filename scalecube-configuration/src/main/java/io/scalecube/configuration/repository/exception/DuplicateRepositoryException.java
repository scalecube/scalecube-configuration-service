package io.scalecube.configuration.repository.exception;

public class DuplicateRepositoryException extends DataAccessException {

  public DuplicateRepositoryException(String message) {
    super(message, null);
  }
}
