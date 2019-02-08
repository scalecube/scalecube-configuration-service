package io.scalecube.configuration.tokens;

import io.scalecube.account.api.GetPublicKeyRequest;
import io.scalecube.account.api.GetPublicKeyResponse;
import io.scalecube.account.api.OrganizationService;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import reactor.core.publisher.Mono;

public final class OrganizationServiceKeyProvider implements KeyProvider {

  private final OrganizationService organizationService;

  /**
   * Creates key provider.
   *
   * @param organizationService organization service.
   */
  public OrganizationServiceKeyProvider(OrganizationService organizationService) {
    this.organizationService = organizationService;
  }

  @Override
  public Mono<Key> get(String keyId) {
    return organizationService
        .getPublicKey(new GetPublicKeyRequest(keyId))
        .map(this::parsePublicKey);
  }

  private PublicKey parsePublicKey(GetPublicKeyResponse publicKeyInfo) {
    try {
      byte[] encodedKey = publicKeyInfo.key();

      EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedKey);

      return KeyFactory.getInstance(publicKeyInfo.algorithm()).generatePublic(keySpec);
    } catch (Exception e) {
      throw new KeyProviderException(e);
    }
  }
}
