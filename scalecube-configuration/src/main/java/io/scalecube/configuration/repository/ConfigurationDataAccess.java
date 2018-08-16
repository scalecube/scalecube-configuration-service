package io.scalecube.configuration.repository;


import java.util.Collection;

public interface ConfigurationDataAccess<T> {

  boolean createRepository(String namespace, String repository);

  T get(String namespace, String repository, String key);

  T put(String namespace, String repository, String key, T doc);

  String remove(String namespace, String repository, String key);

  Collection<T> entries(String namespace, String repository);
}
