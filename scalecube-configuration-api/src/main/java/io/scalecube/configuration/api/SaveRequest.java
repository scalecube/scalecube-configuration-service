package io.scalecube.configuration.api;

import com.fasterxml.jackson.databind.JsonNode;

/** Represents a request to save an entry in a repository. */
public class SaveRequest implements AccessRequest {

  private Object apiKey;
  private String repository;
  private String key;
  private Object value;

  /**
   * Default constructor.
   *
   * @deprecated only for serialization/deserialization.
   */
  SaveRequest() {}

  /**
   * Constructs a save request object.
   *
   * @param apiKey the request apiKey
   * @param repository the repository name
   * @param key the entry key
   * @param value the entry value
   */
  public SaveRequest(Object apiKey, String repository, String key, JsonNode value) {
    this.apiKey = apiKey;
    this.repository = repository;
    this.key = key;
    this.value = value;
  }

  public Object apiKey() {
    return this.apiKey;
  }

  public Object value() {
    return this.value;
  }

  public String key() {
    return this.key;
  }

  public String repository() {
    return this.repository;
  }

  @Override
  public String toString() {
    return "SaveRequest{"
        + "apiKey="
        + apiKey
        + ", repository='"
        + repository
        + '\''
        + ", key='"
        + key
        + '\''
        + ", value="
        + value
        + '}';
  }
}
