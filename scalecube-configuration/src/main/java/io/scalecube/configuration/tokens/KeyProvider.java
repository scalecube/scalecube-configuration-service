package io.scalecube.configuration.tokens;

import java.security.Key;
import reactor.core.publisher.Mono;

/** Public key provider abstraction. Public Key is used to verify signed JWT tokens. */
public interface KeyProvider {

  /**
   * Returns a key corresponding to the <code>keyId</code> argument.
   *
   * @param keyId key identifier.
   * @return key.
   */
  Mono<Key> get(String keyId);
}
