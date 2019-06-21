package io.scalecube.configuration.api;

/** Represents a response to a fetch request. */
public class ReadEntryResponse {

  private Object value;
  private String key;

  /**
   * Default constructor.
   *
   * @deprecated only for serialization/deserialization.
   */
  ReadEntryResponse() {}

  /**
   * Constructs a fetch response object.
   *
   * @param key fetch key
   * @param value fetch value
   */
  public ReadEntryResponse(String key, Object value) {
    this.value = value;
    this.key = key;
  }

  public Object value() {
    return this.value;
  }

  public String key() {
    return key;
  }

  @Override
  public String toString() {
    return "ReadEntryResponse [value=" + value + ", key=" + key + "]";
  }
}
