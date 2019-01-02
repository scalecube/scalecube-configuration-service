package io.scalecube.configuration.tokens;

import java.security.Key;

/**
 * Public key provider abstraction. Public Key is used to verify signed JWT tokens.
 */
public interface KeyProvider {

  /**
   * Returns a key corresponding to the <code>alias</code> argument.
   *
   * @return a cluster
   * @throws KeyProviderException in case of an error while acquiring the cluster.
   */
  Key get(String alias) throws KeyProviderException;
}
