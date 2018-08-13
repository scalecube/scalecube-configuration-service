package io.scalecube.configuration.repository.redis;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import org.junit.jupiter.api.Test;

public class RedisStoreTest {

  private final RedisStore<String> store = new RedisStore<>();

  @Test
  public void test_put_get() {
    store.put("test_put_get", "key1", "1");
    String value = store.get("test_put_get", "key1");
    assertEquals("1", value);
  }

  @Test
  public void test_entries() {
    store.put("test_entries", "key1", "1");
    Collection<String> value = store.entries("test_entries");
    assertTrue(value.size() > 0);
  }

  @Test
  public void test_remove() {
    store.put("test_remove", "key1", "1");
    String value = store.remove("test_remove", "key1");
    Collection<String> entries = store.entries("test_remove");
    assertTrue(entries.size() == 0);
    assertEquals("1", value);
  }
}
