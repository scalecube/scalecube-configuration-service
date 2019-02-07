package io.scalecube.configuration.tokens;

import io.scalecube.account.api.GetPublicKeyRequest;
import io.scalecube.account.api.GetPublicKeyResponse;
import io.scalecube.account.api.OrganizationService;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import reactor.core.publisher.Mono;

class OrganizationServiceKeyProvider implements KeyProvider {

  private static final Map<String, Function<byte[], ? extends EncodedKeySpec>> specProviders =
      new HashMap<>();

  private OrganizationService organizationservice;

  public OrganizationServiceKeyProvider(OrganizationService organizationservice) {
    this.organizationservice = organizationservice;
    byte[] example = new byte[] {};

    Function<byte[], ? extends EncodedKeySpec> x509Spec = X509EncodedKeySpec::new;
    specProviders.put(x509Spec.apply(example).getFormat(), x509Spec);
  }

  @Override
  public Key get(String alias) throws KeyProviderException {
    try {
      return organizationservice
          .getPublicKey(new GetPublicKeyRequest(alias))
          .flatMap(OrganizationServiceKeyProvider::parsePublicKey)
          .block();
    } catch (RuntimeException exception) {
      if (exception.getCause() instanceof KeyProviderException) {
        throw (KeyProviderException) exception.getCause();
      } else {
        throw new KeyProviderException(exception);
      }
    }
  }

  private static Mono<PublicKey> parsePublicKey(GetPublicKeyResponse keyToParse) {
    return Mono.create(
        theKey -> {
          try {
            theKey.success(
                KeyFactory.getInstance(keyToParse.algorithm())
                    .generatePublic(
                        specProviders.get(keyToParse.format()).apply(keyToParse.key())));
          } catch (NoSuchAlgorithmException | InvalidKeySpecException rootCause) {
            theKey.error(new KeyProviderException(rootCause));
          }
        });
  }
}
