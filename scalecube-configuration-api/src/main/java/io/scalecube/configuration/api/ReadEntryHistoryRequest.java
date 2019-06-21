package io.scalecube.configuration.api;

/** Represents a request o fetch data from a repository. */
public class ReadEntryHistoryRequest implements AccessRequest {

  protected String repository;
  protected String key;
  private Object apiKey;

  /**
   * Default constructor.
   *
   * @deprecated only for serialization/deserialization.
   */
  ReadEntryHistoryRequest() {}

  /**
   * Constructs a ReadEntryRequest object.
   *
   * @param apiKey The request apiKey
   * @param repository The repository name
   * @param key The requested data key
   */
  public ReadEntryHistoryRequest(Object apiKey, String repository, String key) {
    this.apiKey = apiKey;
    this.repository = repository;
    this.key = key;
  }

  public String repository() {
    return repository;
  }

  public String key() {
    return key;
  }

  public Object apiKey() {
    return this.apiKey;
  }

  @Override
  public String toString() {
    return "ReadEntryRequest [repository="
        + repository
        + ", key="
        + key
        + ", apiKey="
        + apiKey
        + "]";
  }
}
