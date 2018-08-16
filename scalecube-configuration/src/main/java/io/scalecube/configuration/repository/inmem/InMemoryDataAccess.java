package io.scalecube.configuration.repository.inmem;

import io.scalecube.configuration.repository.ConfigurationDataAccess;
import io.scalecube.configuration.repository.exception.KeyNotFoundException;
import io.scalecube.configuration.repository.exception.NameAlreadyInUseException;
import io.scalecube.configuration.repository.exception.RepositoryNotFoundException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class InMemoryDataAccess implements ConfigurationDataAccess<Object> {

  private final HashMap<String, HashMap<String, HashMap<String, Object>>> map = new HashMap<>();

  @Override
  public boolean createRepository(String namespace, String repository) {
    if (map.containsKey(namespace) && map.get(namespace).containsKey(repository)) {
      throw new NameAlreadyInUseException(repository);
    }
    map.get(namespace).put(repository, new HashMap<>());
    return true;
  }


  @Override
  public Object get(String namespace, String repository, String key) {
    Map<String, Object> map = getRepository(namespace, repository);
    if (map.containsKey(key)) {
      return map.get(key);
    }
    throw new KeyNotFoundException(key);
  }


  @Override
  public Object put(String namespace, String repository, String key, Object value) {
    return getRepository(namespace, repository).put(key, value);
  }

  @Override
  public String remove(String namespace, String repository, String key) {
    getRepository(namespace, repository).remove(key);
    return key;
  }

  @Override
  public Collection<Object> entries(String namespace, String repository) {
    return getRepository(namespace, repository).values();
  }

  private Map<String, Object> getRepository(String namespace, String repository) {
    if (map.containsKey(namespace) && map.get(namespace).containsKey(repository)) {
      return map.get(namespace).get(repository);
    }
    throw new RepositoryNotFoundException(repository);
  }
}
