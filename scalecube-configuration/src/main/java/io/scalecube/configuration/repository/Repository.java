package io.scalecube.configuration.repository;

/**
 * Represents a configuration data repository.
 */
public class Repository {
  private final String namespace;
  private final String name;

  private Repository(Builder builder) {
    this.namespace = builder.namespace;
    this.name = builder.name;
  }

  /**
   * Returns the namespace of the repository.
   * There could be multiple repositories under a certain namespace.
   * @return the repository namespace in string format
   */
  public String namespace() {
    return namespace;
  }

  /**
   * Returns the name of this repository. This value is unique under the namespace.
   * @return the repository name in string format
   */
  public String name() {
    return name;
  }

  /**
   * Returns a builder instance of this {@link Repository}.
   * @return A Builder instance of this class
   */
  public static Builder builder() {
    return new Builder();
  }

  @Override
  public String toString() {
    return super.toString() + String.format("[namespace=%s, name=%s]", namespace, name);
  }

  public static class Builder {
    private String namespace;
    private String name;

    public Builder namespace(String namespace) {
      this.namespace = namespace;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Repository build() {
      return new Repository(this);
    }
  }
}
