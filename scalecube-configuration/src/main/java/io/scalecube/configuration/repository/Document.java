package io.scalecube.configuration.repository;


public class Document {
  private final String id;

  private final String key;

  private final Object value;

  public static final class Builder {
    private String id;

    private String key;

    private Object value;

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder key(String key) {
      this.key = key;
      return this;
    }

    public Builder value(Object value) {
      this.value = value;
      return this;
    }

    public Document build() {
      return new Document(id, key, value);
    }

  }

  public static Builder builder() {
    return new Builder();
  }

  public Document(String id, String key, Object value) {
    this.id = id;
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
    return "Document [key=" + key + ", value=" + value + "]";
  }
}
