package io.scalecube.configuration.repository.exception;

public class InvalidDataAccessResourceUsageException extends DataAccessException {

  public InvalidDataAccessResourceUsageException(String message, Throwable ex) {
    super(message, ex);
  }
}
