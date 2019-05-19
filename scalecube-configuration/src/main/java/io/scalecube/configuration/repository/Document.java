package io.scalecube.configuration.repository;

/** Represents a repository data entity. */
public class Document {

  private String key;
  private Object value;

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

  public String key() {
    return key;
  }

  public Object value() {
    return this.value;
  }

  @Override
  public String toString() {
    return super.toString() + String.format(" [key=%s, value=%s]", key, value);
  }
}
