package io.scalecube.configuration.repository;

public abstract class DataAccessFactory {

  public static ConfigurationDataAccess<Document> getDataAccess() {
    return null;
  }
}
