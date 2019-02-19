package io.scalecube.configuration.tokens;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.scalecube.config.ConfigRegistry;
import io.scalecube.configuration.AppConfiguration;
import java.security.Key;
import java.util.concurrent.TimeUnit;
import reactor.core.publisher.Mono;

public final class CachingKeyProvider implements KeyProvider {

  private final LoadingCache<String, Mono<Key>> cache;

  /**
   * Creates caching key provider.
   *
   * @param delegate provider to delegate calls.
   */
  public CachingKeyProvider(KeyProvider delegate) {
    ConfigRegistry configRegistry = AppConfiguration.configRegistry();

    int cacheSize = configRegistry.intValue("key.cache.max.size", 1000);
    int expiresAfterSeconds = configRegistry.intValue("key.cache.ttl", 300);
    int refreshIntervalSeconds = configRegistry.intValue("key.cache.refresh.interval", 60);

    cache =
        Caffeine.newBuilder()
            .maximumSize(cacheSize)
            .expireAfterWrite(expiresAfterSeconds, TimeUnit.SECONDS)
            .refreshAfterWrite(refreshIntervalSeconds, TimeUnit.SECONDS)
            .build(keyId -> delegate.get(keyId).cache());
  }

  @Override
  public Mono<Key> get(String keyId) {
    return cache.get(keyId);
  }


}
