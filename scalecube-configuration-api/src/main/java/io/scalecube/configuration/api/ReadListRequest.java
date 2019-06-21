package io.scalecube.configuration.api;

public class ReadListRequest implements AccessRequest {

  protected String repository;
  private Object apiKey;

  /**
   * Only for serialization/deserialization.
   *
   * @deprecated for instantiation purposes.
   */
  ReadListRequest() {}

  /**
   * Constructs a ReadListRequest object.
   *
   * @param apiKey The request apiKey
   * @param repository The repository name
   */
  public ReadListRequest(Object apiKey, String repository) {
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
    return "ReadListRequest [repository=" + repository + ", apiKey=" + apiKey + "]";
  }
}
