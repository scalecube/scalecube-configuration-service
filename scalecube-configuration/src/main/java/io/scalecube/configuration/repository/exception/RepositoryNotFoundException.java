package io.scalecube.configuration.repository.exception;

public class RepositoryNotFoundException extends DataAccessException {

  public RepositoryNotFoundException(String message) {
    super(message);
  }

  public RepositoryNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
