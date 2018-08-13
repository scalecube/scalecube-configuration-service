package io.scalecube.configuration;

import java.io.IOException;
import java.util.Properties;

public class AppSettings {

  private final Properties settings;

  private AppSettings() {
    settings = new Properties();

    try {
      settings.load(getClass().getResourceAsStream("/settings.properties"));
    } catch (IOException ex) {
      throw new AppSettingsException("Failed to initialize", ex);
    }
  }

  public static Builder builder() {
    return new AppSettings.Builder();
  }

  public String getProperty(String key) {
    return settings.getProperty(key);
  }

  public static class Builder {
    public AppSettings build() {
      return new AppSettings();
    }
  }
}