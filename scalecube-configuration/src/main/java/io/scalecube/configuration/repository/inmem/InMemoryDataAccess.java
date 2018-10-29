package io.scalecube.configuration.repository.inmem;

import io.scalecube.configuration.repository.ConfigurationDataAccess;
import io.scalecube.configuration.repository.Document;
import io.scalecube.configuration.repository.Repository;
import io.scalecube.configuration.repository.RepositoryEntryKey;
import io.scalecube.configuration.repository.exception.DuplicateRepositoryException;
import io.scalecube.configuration.repository.exception.KeyNotFoundException;
import io.scalecube.configuration.repository.exception.RepositoryNotFoundException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class InMemoryDataAccess implements ConfigurationDataAccess {

  private final HashMap<String, HashMap<String, HashMap<String, Document>>> map = new HashMap<>();

  @Override
  public boolean createRepository(Repository repository) {
    if (repositoryExists(repository)) {
      throw new DuplicateRepositoryException(repository.toString());
    }

    map.putIfAbsent(repository.namespace(), new HashMap<>());
    map.get(repository.namespace()).put(repository.name(), new HashMap<>());
    return true;
  }


  @Override
  public Document get(RepositoryEntryKey key) {
    Map<String, Document> map = getRepository(key.repository());
    if (map.containsKey(key.key())) {
      return map.get(key.key());
    }
    throw new KeyNotFoundException(key.key());
  }


  @Override
  public Document put(RepositoryEntryKey key, Document value) {
    return getRepository(key.repository()).put(key.key(), value);
  }

  @Override
  public String remove(RepositoryEntryKey key) {
    Map<String, Document> map = getRepository(key.repository());
    if (map.containsKey(key.key())) {
      map.remove(key.key());
      return key.key();
    }
    throw new KeyNotFoundException(key.toString());
  }

  @Override
  public Collection<Document> entries(Repository repository) {
    return getRepository(repository).values();
  }

  private Map<String, Document> getRepository(Repository repository) {
    repositoryExists(repository);

    if (repositoryExists(repository)) {
      return map.get(repository.namespace()).get(repository.name());
    }
    throw new RepositoryNotFoundException(repository.toString());
  }

  private boolean repositoryExists(Repository repository) {
    return map.containsKey(repository.namespace())
      && map.get(repository.namespace()).containsKey(repository.name());
  }
}
