package io.scalecube.configuration.repository.exception;

public class OperationInterruptedException extends DataAccessException {

  public OperationInterruptedException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
