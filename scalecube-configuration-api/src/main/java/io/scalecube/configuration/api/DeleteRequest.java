package io.scalecube.configuration.api;

/** Represents a request to delete a repository entry. */
public class DeleteRequest implements AccessRequest {

  private Object token;
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
   * @param token request token
   * @param repository repository name
   * @param key entry to delete
   */
  public DeleteRequest(Object token, String repository, String key) {
    this.token = token;
    this.repository = repository;
    this.key = key;
  }

  public Object token() {
    return token;
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
        + "token="
        + token
        + ", repository='"
        + repository
        + '\''
        + ", key='"
        + key
        + '\''
        + '}';
  }
}
