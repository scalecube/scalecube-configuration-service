package io.scalecube.configuration.repository.couchbase;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

/** Default Jackson implementation of <code>TranslationService</code>. */
public class JacksonTranslationService implements TranslationService {

  private static final ObjectMapper objectMapper =
      new ObjectMapper().setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

  public static ObjectMapper objectMapper() {
    return objectMapper;
  }

  @Override
  public <T> byte[] encode(T source, Class<T> sourceType) {
    try {
      return objectMapper.writerFor(sourceType).writeValueAsBytes(source);
    } catch (IOException ex) {
      throw new RuntimeException("Could not encode JSON", ex);
    }
  }

  @Override
  public <T> T decode(byte[] source, Class<T> targetType) {
    try {
      return objectMapper.readerFor(targetType).readValue(source);
    } catch (IOException ex) {
      throw new RuntimeException("Could not decode JSON", ex);
    }
  }
}
