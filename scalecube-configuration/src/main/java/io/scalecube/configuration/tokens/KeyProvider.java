package io.scalecube.configuration.tokens;

import java.io.IOException;

import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;

/**
 * Public key provider abstraction. Public key is used to verify signed JWT tokens.
 */
public interface KeyProvider {

  /**
   * Returns a key corresponding to the <code>alias</code> argument.
   *
   * @return a key
   * @throws KeyProviderException in case of an error while acquiring the key.
   */
  Key get(String alias) throws KeyProviderException;
}
