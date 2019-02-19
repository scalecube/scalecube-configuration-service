package io.scalecube.configuration.repository;

/**
 * Represents a configuration data repository.
 */
public class Repository {
  private final String namespace;
  private final String name;

  public Repository(String namespace, String name) {
    this.namespace = namespace;
    this.name = name;
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

  @Override
  public String toString() {
    return super.toString() + String.format("[namespace=%s, name=%s]", namespace, name);
  }


}
