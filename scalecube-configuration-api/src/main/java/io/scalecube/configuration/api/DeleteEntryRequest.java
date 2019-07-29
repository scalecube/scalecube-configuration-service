package io.scalecube.configuration.api;

/** Represents a request to delete a repository entry. */
public class DeleteEntryRequest implements AccessRequest {

  private Object apiKey;
  private String repository;
  private String key;

  /**
   * Default constructor.
   *
   * @deprecated only for serialization/deserialization.
   */
  DeleteEntryRequest() {}

  /**
   * Constructs a delete entry request object.
   *
   * @param apiKey request apiKey
   * @param repository repository name
   * @param key entry to delete
   */
  public DeleteEntryRequest(Object apiKey, String repository, String key) {
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
    return "DeleteEntryRequest{"
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
