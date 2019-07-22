package io.scalecube.configuration.api;

public class ReadListRequest implements AccessRequest {

  protected String repository;
  private Object apiKey;
  private Object version;

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

  /**
   * Constructs a ReadListRequest object.
   *
   * @param apiKey The request apiKey
   * @param repository The repository name
   * @param version The version for keys of repository
   */
  public ReadListRequest(Object apiKey, String repository, Object version) {
    this.apiKey = apiKey;
    this.repository = repository;
    this.version = version;
  }

  public String repository() {
    return repository;
  }

  public Object apiKey() {
    return this.apiKey;
  }

  public Object version() {
    return this.version;
  }

  @Override
  public String toString() {
    return "ReadListRequest [repository="
        + repository
        + ", apiKey="
        + apiKey
        + ", version="
        + version
        + "]";
  }
}
