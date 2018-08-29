package io.scalecube.configuration.tokens;

public class KeyStoreException extends Exception {

  public KeyStoreException(String message) {
    super(message);
  }

  public KeyStoreException(Exception ex) {
    super(ex);
  }
}
