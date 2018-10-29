package io.scalecube.configuration.repository;

/**
 * Represents a composite entry key.
 */
public class RepositoryEntryKey {
  private final Repository repository;
  private final String key;

  private RepositoryEntryKey(Builder builder) {
    this.repository = builder.repository;
    this.key = builder.key;
  }

  public Repository repository() {
    return repository;
  }

  public String key() {
    return key;
  }

  public static Builder builder() {
    return new Builder();
  }


  @Override
  public String toString() {
    return super.toString() + String.format("[name=%s, key=%s]", repository, key);
  }

  public static class Builder {
    private Repository repository;
    private String key;

    public Builder repository(Repository repository) {
      this.repository = repository;
      return this;
    }

    public Builder key(String key) {
      this.key = key;
      return this;
    }

    public RepositoryEntryKey build() {
      return new RepositoryEntryKey(this);
    }
  }
}
