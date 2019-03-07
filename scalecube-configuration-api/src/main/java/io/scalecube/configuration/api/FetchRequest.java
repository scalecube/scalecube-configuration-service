package io.scalecube.configuration.api;

/** Represents a request o fetch data from a repository. */
public class FetchRequest implements AccessRequest {

  protected String repository;
  protected String key;
  private Object token;

  /**
   * Default constructor.
   *
   * @deprecated only for serialization/deserialization.
   */
  FetchRequest() {}

  /**
   * Constructs a FetchRequest object.
   *
   * @param token The request token
   * @param repository The repository name
   * @param key The requested data key
   */
  public FetchRequest(Object token, String repository, String key) {
    this.token = token;
    this.repository = repository;
    this.key = key;
  }

  /**
   * Constructs a FetchRequest object.
   *
   * @param token The request token
   * @param repository The repository name
   */
  public FetchRequest(Object token, String repository) {
    this(token, repository, null);
  }

  public String repository() {
    return repository;
  }

  public String key() {
    return key;
  }

  public Object token() {
    return this.token;
  }

  @Override
  public String toString() {
    return "FetchRequest [repository=" + repository + ", key=" + key + ", token=" + token + "]";
  }
}
