package io.scalecube.configuration.api;

public class EntriesRequest implements AccessRequest {

  protected String repository;
  private Object apiKey;

  /**
   * Only for serialization/deserialization.
   *
   * @deprecated for instantiation purposes.
   */
  EntriesRequest() {}

  /**
   * Constructs a EntriesRequest object.
   *
   * @param apiKey The request apiKey
   * @param repository The repository name
   */
  public EntriesRequest(Object apiKey, String repository) {
    this.apiKey = apiKey;
    this.repository = repository;
  }

  public String repository() {
    return repository;
  }

  public Object apiKey() {
    return this.apiKey;
  }

  @Override
  public String toString() {
    return "EntriesRequest [repository=" + repository + ", apiKey=" + apiKey + "]";
  }
}
