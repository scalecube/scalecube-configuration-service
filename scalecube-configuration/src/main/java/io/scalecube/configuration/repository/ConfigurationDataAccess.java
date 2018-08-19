package io.scalecube.configuration.repository;


import java.util.Collection;

public interface ConfigurationDataAccess {

  boolean createRepository(String namespace, String repository);

  Document get(String namespace, String repository, String key);

  Document put(String namespace, String repository, String key, Document document);

  String remove(String namespace, String repository, String key);

  Collection<Document> entries(String namespace, String repository);
}
