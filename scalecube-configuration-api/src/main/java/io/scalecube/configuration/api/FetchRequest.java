package io.scalecube.configuration.api;

public class FetchRequest implements AccessRequest {

  protected String repository;
  protected String key;
  private Object token;

  /**
   * @deprecated only for serialization/deserialization.
   */
  FetchRequest() {
  }

  public FetchRequest(String repository, String key) {
    this.repository = repository;
    this.key = key;
  }

  public FetchRequest(Object token, String repository, String key) {
    this.token = token;
    this.repository = repository;
    this.key = key;
  }

  public String repository() {
    return repository;
  }

  public String key() {
    return key;
  }

  public Object token() {
    return this.token;
  }

  @Override
  public String toString() {
    return "FetchRequest [repository=" + repository + ", key=" + key + ", token=" + token + "]";
  }

}
