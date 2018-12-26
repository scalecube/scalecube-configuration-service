package io.scalecube.configuration.repository;

/**
 * Represents a composite entry cluster.
 */
public class RepositoryEntryKey {
  private final Repository repository;
  private final String key;

  private RepositoryEntryKey(Builder builder) {
    this.repository = builder.repository;
    this.key = builder.key;
  }

  /**
   * Returns the repository of this {@link RepositoryEntryKey}.
   * @return the repository of this {@link RepositoryEntryKey}
   */
  public Repository repository() {
    return repository;
  }

  /**
   * Returns the key of this {@link RepositoryEntryKey}.
   * @return the key of this {@link RepositoryEntryKey}.
   */
  public String key() {
    return key;
  }

  /**
   * Returns a Builder instance of this {@link RepositoryEntryKey}.
   * @return a Builder instance of this {@link RepositoryEntryKey}
   */
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
