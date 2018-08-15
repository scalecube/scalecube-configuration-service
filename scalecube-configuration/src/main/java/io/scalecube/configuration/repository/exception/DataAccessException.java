package io.scalecube.configuration.repository.exception;

public class DataAccessException extends RuntimeException {

  public DataAccessException(String message, Throwable cause) {

    super(message, cause);
  }

  public DataAccessException(String message) {
    this(message, null);
  }
}
