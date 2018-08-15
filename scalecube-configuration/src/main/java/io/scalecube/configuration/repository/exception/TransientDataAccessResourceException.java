package io.scalecube.configuration.repository.exception;

public class TransientDataAccessResourceException extends DataAccessException {

  public TransientDataAccessResourceException(String message, Throwable ex) {
    super(message, ex);
  }
}
