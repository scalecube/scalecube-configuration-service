package io.scalecube.configuration.repository;

import io.scalecube.configuration.repository.exception.DataAccessException;
import io.scalecube.configuration.repository.inmem.InMemoryDataAccess;

public abstract class DataAccessFactory {
  public static ConfigurationDataAccess getDataAccess() {
    return new InMemoryDataAccess();
  }
}
