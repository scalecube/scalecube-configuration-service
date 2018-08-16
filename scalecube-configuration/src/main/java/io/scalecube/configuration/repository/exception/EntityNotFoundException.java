package io.scalecube.configuration.repository.exception;

public class EntityNotFoundException extends DataAccessException {

  public EntityNotFoundException(String id) {
    super(id);
  }
}
