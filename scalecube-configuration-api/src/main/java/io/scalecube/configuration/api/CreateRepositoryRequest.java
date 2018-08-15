package io.scalecube.configuration.api;

public class CreateRepositoryRequest implements AccessRequest {

  private Object token;
  private String repository;

  /**
   * @deprecated only for serialization/deserialization.
   */
  CreateRepositoryRequest() {
  }

  public CreateRepositoryRequest(Object token, String repository) {
    this.repository = repository;
    this.token = token;
  }

  @Override
  public String repository() {
    return repository;
  }

  @Override
  public Object token() {
    return token;
  }
}
