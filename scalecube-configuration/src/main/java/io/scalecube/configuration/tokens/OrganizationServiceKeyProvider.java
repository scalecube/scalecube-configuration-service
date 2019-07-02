package io.scalecube.configuration.tokens;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.scalecube.account.api.GetPublicKeyRequest;
import io.scalecube.account.api.GetPublicKeyResponse;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.config.ConfigRegistry;
import io.scalecube.configuration.AppConfiguration;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.concurrent.TimeUnit;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public final class OrganizationServiceKeyProvider {

  ConfigRegistry configRegistry = AppConfiguration.configRegistry();

  int cacheSize = configRegistry.intValue("key.cache.max.size", 1000);
  int expiresAfterSeconds = configRegistry.intValue("key.cache.ttl", 300);
  int refreshIntervalSeconds = configRegistry.intValue("key.cache.refresh.interval", 60);
  private final LoadingCache<String, Mono<Key>> cache;

  /**
   * Creates key provider.
   *
   * @param organizationService organization service.
   */
  public OrganizationServiceKeyProvider(OrganizationService organizationService) {
    cache =
        Caffeine.newBuilder()
            .maximumSize(cacheSize)
            .expireAfterWrite(expiresAfterSeconds, TimeUnit.SECONDS)
            .refreshAfterWrite(refreshIntervalSeconds, TimeUnit.SECONDS)
            .build(
                keyId ->
                    organizationService
                        .getPublicKey(new GetPublicKeyRequest(keyId))
                        .publishOn(Schedulers.parallel())
                        .map(OrganizationServiceKeyProvider::parsePublicKey)
                        .cache());
  }

  public Mono<Key> get(String keyId) {
    return cache.get(keyId);
  }

  private static Key parsePublicKey(GetPublicKeyResponse publicKeyInfo) {
    try {
      byte[] encodedKey = publicKeyInfo.key();

      EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedKey);

      return KeyFactory.getInstance(publicKeyInfo.algorithm()).generatePublic(keySpec);
    } catch (Exception e) {
      throw new KeyProviderException(e);
    }
  }
}
