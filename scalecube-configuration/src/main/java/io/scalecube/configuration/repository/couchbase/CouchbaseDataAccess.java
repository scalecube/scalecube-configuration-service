package io.scalecube.configuration.repository.couchbase;

import io.scalecube.configuration.repository.ConfigurationDataAccess;
import io.scalecube.configuration.repository.Document;
import java.util.Collection;

public class CouchbaseDataAccess implements ConfigurationDataAccess<Document> {

  @Override
  public void createRepository(String namespace, String repository) {
    
  }

  @Override
  public Document get(String namespace, String repository, String key) {
    return null;
  }

  @Override
  public Document put(String namespace, String repository, String key, Document doc) {
    return null;
  }

  @Override
  public Document remove(String namespace, String repository, String key) {
    return null;
  }

  @Override
  public Collection<Document> entries(String namespace, String repository) {
    return null;
  }
}
