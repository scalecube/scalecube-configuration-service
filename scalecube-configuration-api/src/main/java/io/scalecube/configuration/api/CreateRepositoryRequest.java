package io.scalecube.configuration.api;

/** Represents a request to create a repository. */
public class CreateRepositoryRequest implements AccessRequest {

  private Object apiKey;
  private String repository;

  /**
   * Default constructor.
   *
   * @deprecated only for serialization/deserialization.
   */
  CreateRepositoryRequest() {}

  /**
   * Constructs a request object.
   *
   * @param apiKey request apiKey
   * @param repository the new repository name
   */
  public CreateRepositoryRequest(Object apiKey, String repository) {
    this.repository = repository;
    this.apiKey = apiKey;
  }

  @Override
  public String repository() {
    return repository;
  }

  @Override
  public Object apiKey() {
    return apiKey;
  }

  @Override
  public String toString() {
    return "CreateRepositoryRequest{"
        + "apiKey="
        + apiKey
        + ", repository='"
        + repository
        + '\''
        + '}';
  }
}
