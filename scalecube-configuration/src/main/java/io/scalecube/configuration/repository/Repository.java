package io.scalecube.configuration.repository;

/**
 * Represents a name.
 */
public class Repository {
  private final String namespace;
  private final String name;

  private Repository(Builder builder) {
    this.namespace = builder.namespace;
    this.name = builder.name;
  }

  public String namespace() {
    return namespace;
  }

  public String name() {
    return name;
  }

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
