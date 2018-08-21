package io.scalecube.configuration.api;

/**
 * Represents a collection of objects.
 * @param <T> this entries underlying object type
 */
public class Entries<T> {

  private T[] entries;

  /**
   * Default constructor.
   * @deprecated only for serialization/deserialization.
   */
  Entries() {
  }

  /**
   * Constructs an instance of entries.
   * @param array This entries object items.
   */
  public Entries(T[] array) {
    this.entries = array;
  }

  public T[] entries() {
    return this.entries;
  }
}
