package io.scalecube.configuration.api;


import com.fasterxml.jackson.databind.JsonNode;

public class SaveRequest implements AccessRequest {

  private Object token;
  private String repository;
  private String key;
  private JsonNode value;

  /**
   * @deprecated only for serialization/deserialization.
   */
  SaveRequest() {
  }

  public SaveRequest(String repository, String key, JsonNode value) {
    this.repository = repository;
    this.key = key;
    this.value = value;
  }

  public SaveRequest(Object token, String repository, String key, JsonNode value) {
    this.token = token;
    this.repository = repository;
    this.key = key;
    this.value = value;
  }

  public Object token() {
    return this.token;
  }

  public JsonNode value() {
    return this.value;
  }

  public String key() {
    return this.key;
  }

  public String repository() {
    return this.repository;
  }
}
