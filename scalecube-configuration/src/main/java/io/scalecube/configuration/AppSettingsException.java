package io.scalecube.configuration;

import java.io.IOException;

public class AppSettingsException extends RuntimeException {

  public AppSettingsException(String message, Throwable cause) {
    super(message, cause);
  }
}
