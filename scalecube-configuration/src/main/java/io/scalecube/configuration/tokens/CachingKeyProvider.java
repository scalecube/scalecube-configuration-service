package io.scalecube.configuration.tokens;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import java.security.Key;
import java.util.concurrent.TimeUnit;

class CachingKeyProvider implements KeyProvider {

  private final LoadingCache<String, Key> cache;

  CachingKeyProvider(KeyProvider delegate) {
    cache =
        Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .refreshAfterWrite(1, TimeUnit.MINUTES)
            .build(delegate::get);
  }

  @Override
  public Key get(String alias) throws KeyProviderException {
    return cache.get(alias);
  }
}
