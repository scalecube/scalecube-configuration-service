package io.scalecube.configuration.tokens;

import java.io.IOException;

import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;

/**
 * Public key provider abstraction. Public key is used to verify signed JWT tokens.
 */
public interface PublicKeyProvider {

  /**
   * Returns the system public key.
   *
   * @return a Public key
   * @throws IOException in case of an error while acquiring the key.
   * @throws KeyStoreException in case of an error while acquiring the key.
   * @throws CertificateException in case of an error while  acquiring the key.
   * @throws NoSuchAlgorithmException in case of an error while acquiring the key.
   */
  PublicKey get()
      throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException;
}
