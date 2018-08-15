package io.scalecube.configuration.repository.exception;

public class RepositoryNotFoundException extends DataAccessException {

  public RepositoryNotFoundException(String message) {

    super(message);
  }
}
