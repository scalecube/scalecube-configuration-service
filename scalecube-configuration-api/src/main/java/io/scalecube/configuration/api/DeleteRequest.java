package io.scalecube.configuration.api;

public class DeleteRequest implements AccessRequest {

  private Object token;
  private String collection;
  private String key;

  /**
   * @deprecated only for serialization/deserialization
   */
  DeleteRequest() {}

  public DeleteRequest(Object token, String collection, String key) {
    this.token = token;
    this.collection = collection;
    this.key = key;
  }

  public Object token() {
    return token;
  }

  public String repository() {
    return collection;
  }

  public String key() {
    return key;
  }

}
