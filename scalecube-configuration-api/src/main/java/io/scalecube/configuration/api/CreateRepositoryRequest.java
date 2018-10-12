package io.scalecube.configuration.api;

/** Represents a request to create a repository. */
public class CreateRepositoryRequest implements AccessRequest {

  private Object token;
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
   * @param token request token
   * @param repository the new repository name
   */
  public CreateRepositoryRequest(Object token, String repository) {
    this.repository = repository;
    this.token = token;
  }

  @Override
  public String repository() {
    return repository;
  }

  @Override
  public Object token() {
    return token;
  }

  @Override
  public String toString() {
    return "CreateRepositoryRequest{"
        + "token="
        + token
        + ", repository='"
        + repository
        + '\''
        + '}';
  }
}
