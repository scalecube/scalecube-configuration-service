package io.scalecube.configuration.repository;

import io.scalecube.configuration.repository.exception.DataAccessException;
import java.util.Collection;

public interface ConfigurationDataAccess<T> {

  void createRepository(String namespace, String repository) throws DataAccessException;

  T get(String namespace, String repository, String key) throws DataAccessException;
  T put(String namespace, String repository, String key, T doc) throws DataAccessException;
  T remove(String namespace, String repository, String key) throws DataAccessException;
  Collection<T> entries(String namespace, String repository) throws DataAccessException;
}
