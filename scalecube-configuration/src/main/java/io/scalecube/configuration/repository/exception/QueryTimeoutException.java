package io.scalecube.configuration.repository.exception;

public class QueryTimeoutException extends DataAccessException {

  public QueryTimeoutException(String message, Throwable ex) {
    super(message, ex);
  }
}
