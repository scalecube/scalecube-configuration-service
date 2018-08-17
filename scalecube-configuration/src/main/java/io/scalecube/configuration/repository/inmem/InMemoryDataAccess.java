package io.scalecube.configuration.repository.inmem;

import io.scalecube.configuration.repository.ConfigurationDataAccess;
import io.scalecube.configuration.repository.Document;
import io.scalecube.configuration.repository.exception.KeyNotFoundException;
import io.scalecube.configuration.repository.exception.NameAlreadyInUseException;
import io.scalecube.configuration.repository.exception.RepositoryNotFoundException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.print.Doc;

public class InMemoryDataAccess implements ConfigurationDataAccess {

  private final HashMap<String, HashMap<String, HashMap<String, Document>>> map = new HashMap<>();

  @Override
  public boolean createRepository(String namespace, String repository) {
    if (map.containsKey(namespace) && map.get(namespace).containsKey(repository)) {
      throw new NameAlreadyInUseException(repository);
    }
    map.putIfAbsent(namespace, new HashMap<>());
    map.get(namespace).put(repository, new HashMap<>());
    return true;
  }


  @Override
  public Document get(String namespace, String repository, String key) {
    Map<String, Document> map = getRepository(namespace, repository);
    if (map.containsKey(key)) {
      return map.get(key);
    }
    throw new KeyNotFoundException(key);
  }


  @Override
  public Document put(String namespace, String repository, String key, Document value) {
    return getRepository(namespace, repository).put(key, value);
  }

  @Override
  public String remove(String namespace, String repository, String key) {
    getRepository(namespace, repository).remove(key);
    return key;
  }

  @Override
  public Collection<Document> entries(String namespace, String repository) {
    return getRepository(namespace, repository).values();
  }

  private Map<String, Document> getRepository(String namespace, String repository) {
    if (map.containsKey(namespace) && map.get(namespace).containsKey(repository)) {
      return map.get(namespace).get(repository);
    }
    throw new RepositoryNotFoundException(repository);
  }
}
