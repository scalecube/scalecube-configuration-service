package io.scalecube.configuration.repository.exception;

public class DataAccessResourceFailureException extends DataAccessException {

  public DataAccessResourceFailureException(String message, Throwable cause) {
    super(message, cause);
  }
}
