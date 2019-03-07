package io.scalecube.configuration.repository.couchbase;

/** Represents a translation service from object to byte array and vice versa. */
public interface TranslationService {

  /**
   * Encodes an object into the byte array.
   *
   * @param source the source contents to encode.
   * @return the encoded document representation.
   */
  <T> byte[] encode(T source, Class<T> sourceType);

  /**
   * Decodes the byte array into the target
   *
   * @param source the source formatted document.
   * @return a properly populated object to work with.
   */
  <T> T decode(byte[] source, Class<T> targetType);
}
