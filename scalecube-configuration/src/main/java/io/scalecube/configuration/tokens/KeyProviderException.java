package io.scalecube.configuration.tokens;

public class KeyProviderException extends RuntimeException {

  KeyProviderException(String message) {
    this(message, null);
  }

  KeyProviderException(Exception cause) {
    this(null, cause);
  }

  KeyProviderException(String message, Exception ex) {
    super(message, ex);
  }
}
