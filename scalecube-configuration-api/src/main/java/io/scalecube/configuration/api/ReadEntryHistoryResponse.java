package io.scalecube.configuration.api;

/** Represents a response to a key version history request. */
public class ReadEntryHistoryResponse {

  private Integer version;
  private Object value;

  /**
   * Default constructor.
   *
   * @deprecated only for serialization/deserialization.
   */
  ReadEntryHistoryResponse() {}

  /**
   * Constructs a fetch response object.
   *
   * @param version key version
   * @param value key version value
   */
  public ReadEntryHistoryResponse(Integer version, Object value) {
    this.version = version;
    this.value = value;
  }

  public Integer version() {
    return version;
  }

  public Object value() {
    return this.value;
  }

  @Override
  public String toString() {
    return "ReadEntryHistoryResponse [version=" + version + ", value=" + value + "]";
  }
}
