package io.scalecube.configuration.repository;

import io.scalecube.configuration.repository.couchbase.CouchbaseDataAccess;

public abstract class DataAccessFactory {

  public static ConfigurationDataAccess dataAccess() {
    return new CouchbaseDataAccess();
  }
}
