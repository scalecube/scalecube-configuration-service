package io.scalecube.configuration.api;

public class DeleteRequest implements AccessRequest {

  private Object token;
  private String repository;
  private String key;

  /**
   * @deprecated only for serialization/deserialization.
   */
  DeleteRequest() {
  }

  public DeleteRequest(Object token, String repository, String key) {
    this.token = token;
    this.repository = repository;
    this.key = key;
  }

  public Object token() {
    return token;
  }

  public String repository() {
    return repository;
  }

  public String key() {
    return key;
  }

}
