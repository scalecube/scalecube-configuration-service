package io.scalecube.configuration.tokens;

import java.io.IOException;

public class KeyProviderException extends Exception {

  public KeyProviderException(String message) {
    super(message);
  }

  public KeyProviderException(Exception cause) {
    super(cause);
  }
}
