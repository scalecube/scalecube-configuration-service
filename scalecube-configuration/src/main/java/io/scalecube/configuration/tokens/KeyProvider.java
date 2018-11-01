package io.scalecube.configuration.tokens;

import java.io.IOException;

import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;

/**
 * Public cluster provider abstraction. Public cluster is used to verify signed JWT tokens.
 */
public interface KeyProvider {

  /**
   * Returns a cluster corresponding to the <code>alias</code> argument.
   *
   * @return a cluster
   * @throws KeyProviderException in case of an error while acquiring the cluster.
   */
  Key get(String alias) throws KeyProviderException;
}
