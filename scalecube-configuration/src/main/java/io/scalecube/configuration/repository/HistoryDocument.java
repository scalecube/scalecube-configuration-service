package io.scalecube.configuration.repository;

/** Represents a repository key versions. */
public class HistoryDocument {

  private Integer version;
  private Object value;

  /**
   * Constructs a document object.
   *
   * @param version key version
   * @param value entry value
   */
  public HistoryDocument(Integer version, Object value) {
    this.version = version;
    this.value = value;
  }

  public Integer version() {
    return this.version;
  }

  public Object value() {
    return this.value;
  }

  @Override
  public String toString() {
    return super.toString() + String.format(" [version=%s, value=%s]", version, value);
  }
}
