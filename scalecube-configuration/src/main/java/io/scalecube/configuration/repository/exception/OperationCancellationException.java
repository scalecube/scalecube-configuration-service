package io.scalecube.configuration.repository.exception;

public class OperationCancellationException extends DataAccessException {

  public OperationCancellationException(String message, Throwable ex) {
    super(message, ex);
  }
}
