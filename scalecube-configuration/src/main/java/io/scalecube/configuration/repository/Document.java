package io.scalecube.configuration.repository;

/** Represents a repository data entity. */
public class Document {

  private String key;
  private Object value;
  private Integer version;

  /**
   * Constructs a document object.
   *
   * @param key entry cluster
   * @param value entry value
   */
  public Document(String key, Object value) {
    this.key = key;
    this.value = value;
  }

  /**
   * Constructs a document object.
   *
   * @param key entry cluster
   * @param value entry value
   * @param version entry version
   */
  public Document(String key, Object value, Integer version) {
    this.key = key;
    this.value = value;
    this.version = version;
  }

  public String key() {
    return key;
  }

  public Object value() {
    return this.value;
  }

  public Integer version() {
    return this.version;
  }

  @Override
  public String toString() {
    return super.toString() + String.format(" [key=%s, value=%s]", key, value);
  }
}
