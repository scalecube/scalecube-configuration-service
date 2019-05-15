package io.scalecube.configuration.api;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents a response to a fetch request.
 */
public class FetchResponse {

  private JsonNode value;
  private String key;

  /**
   * Default constructor.
   *
   * @deprecated only for serialization/deserialization.
   */
  FetchResponse() {
  }

  /**
   * Constructs a fetch response object.
   *
   * @param key fetch key
   * @param value fetch value
   */
  public FetchResponse(String key, JsonNode value) {
    this.value = value;
    this.key = key;
  }

  public JsonNode value() {
    return this.value;
  }

  public String key() {
    return key;
  }

  @Override
  public String toString() {
    return "FetchResponse [value=" + value + ", key=" + key + "]";
  }

}
