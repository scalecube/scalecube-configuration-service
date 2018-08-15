package io.scalecube.configuration.repository;


import java.util.Collection;

public interface ConfigurationDataAccess<T> {

  void createRepository(String namespace, String repository);

  T get(String namespace, String repository, String key);

  T put(String namespace, String repository, String key, T doc);

  T remove(String namespace, String repository, String key);

  Collection<T> entries(String namespace, String repository);
}
