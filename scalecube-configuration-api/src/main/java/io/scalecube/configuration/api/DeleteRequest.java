package io.scalecube.configuration.api;

/** Represents a request to delete a repository entry. */
public class DeleteRequest implements AccessRequest {

  private Object apiKey;
  private String repository;
  private String key;

  /**
   * Default constructor.
   *
   * @deprecated only for serialization/deserialization.
   */
  DeleteRequest() {}

  /**
   * Constructs a delete entry request object.
   *
   * @param apiKey request apiKey
   * @param repository repository name
   * @param key entry to delete
   */
  public DeleteRequest(Object apiKey, String repository, String key) {
    this.apiKey = apiKey;
    this.repository = repository;
    this.key = key;
  }

  public Object apiKey() {
    return apiKey;
  }

  public String repository() {
    return repository;
  }

  public String key() {
    return key;
  }

  @Override
  public String toString() {
    return "DeleteRequest{"
        + "apiKey="
        + apiKey
        + ", repository='"
        + repository
        + '\''
        + ", key='"
        + key
        + '\''
        + '}';
  }
}
