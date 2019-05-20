package io.scalecube.config.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.scalecube.config.ConfigRegistry;
import io.scalecube.config.utils.ThrowableUtil;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

/**
 * Holder class for {@link ObjectMapper}. The {@link ObjectMapper} is
 *
 * <ul>
 *   <li>using any accessor with any visibility to read / write your objects
 *   <li>supporting java 8 time and {@link Optional}s
 *   <li>not failing on unknown properties
 *   <li>not failing on empty values
 *   <li>putting null on any unknown value of enums
 *   <li>not writing (using {@link ObjectMapper#writer() or similar methods} dates as timestamp
 *   <li>writing (using {@link ObjectMapper#writer() or similar methods} enums as strings
 * </ul>
 *
 * @see Jdk8Module
 * @see JavaTimeModule
 * @see DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
 * @see SerializationFeature.FAIL_ON_EMPTY_BEANS
 * @see DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL
 * @see SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
 * @see SerializationFeature.WRITE_ENUMS_USING_TO_STRING
 */
public class ObjectMapperHolder {

  private static ObjectMapper objectMapper = initMapper();

  private static ObjectMapper initMapper() {
    ObjectMapper mapper =
        new ObjectMapper().registerModule(new Jdk8Module()).registerModule(new JavaTimeModule());
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);

    mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    return mapper;
  }

  public static ObjectMapper getInstance() {
    return objectMapper;
  }

  /**
   * Parse a Json into type. This is a helper method for config registry.
   *
   * @param toType the type the json represents
   * @return a function that will parse the json
   * @see ConfigRegistry#objectProperty(String, Function)
   */
  public static <T> Function<String, T> parseJsonAs(Class<? extends T> toType) {
    return src -> {
      try {
        return objectMapper.readerFor(toType).readValue(src);
      } catch (IOException exception) {
        throw ThrowableUtil.propagate(exception);
      }
    };
  }
}
