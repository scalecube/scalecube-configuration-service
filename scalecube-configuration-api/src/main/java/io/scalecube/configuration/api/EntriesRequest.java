package io.scalecube.configuration.api;

public class EntriesRequest implements AccessRequest {

  protected String repository;
  private Object token;

  /**
   * Only for serialization/deserialization.
   *
   * @deprecated for instantiation purposes.
   */
  EntriesRequest() {}

  /**
   * Constructs a EntriesRequest object.
   *
   * @param token The request token
   * @param repository The repository name
   */
  public EntriesRequest(Object token, String repository) {
    this.token = token;
    this.repository = repository;
  }

  public String repository() {
    return repository;
  }

  public Object token() {
    return this.token;
  }

  @Override
  public String toString() {
    return "EntriesRequest [repository=" + repository + ", token=" + token + "]";
  }
}
