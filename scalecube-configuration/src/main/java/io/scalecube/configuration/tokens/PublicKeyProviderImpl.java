package io.scalecube.configuration.tokens;

import io.scalecube.configuration.AppSettings;

import java.io.FileInputStream;
import java.io.IOException;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Objects;

public class PublicKeyProviderImpl implements PublicKeyProvider {
  private PublicKey key;

  @Override
  public PublicKey get()
      throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
    return key == null ? getKey0() : key;
  }

  private PublicKey getKey0()
      throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
    AppSettings settings = AppSettings.builder().build();
    String keyStoreType = settings.getProperty("token.keystore.type");
    String keyStorePath = settings.getProperty("token.keystore");
    char[] keyStorePass = settings.getProperty("token.keystore.pass").toCharArray();
    String alias = settings.getProperty("token.key.alias");
    KeyStore keyStore = KeyStore.getInstance(keyStoreType);

    keyStore.load(
        new FileInputStream(keyStorePath), keyStorePass);
    Certificate certificate = keyStore.getCertificate(alias);
    Objects.requireNonNull(certificate,
        String.format("certificate with alias: '%s' not found in keystore: '%s'", alias,
            keyStorePath));
    return certificate.getPublicKey();
  }
}
