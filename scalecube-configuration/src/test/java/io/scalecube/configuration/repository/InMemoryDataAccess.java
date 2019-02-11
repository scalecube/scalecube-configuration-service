package io.scalecube.configuration.repository;

import io.scalecube.configuration.repository.exception.DuplicateRepositoryException;
import io.scalecube.configuration.repository.exception.KeyNotFoundException;
import io.scalecube.configuration.repository.exception.RepositoryNotFoundException;
import java.util.HashMap;
import java.util.Map;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class InMemoryDataAccess implements ConfigurationDataAccess {

  private final Map<String, Map<String, Map<String, Document>>> map = new HashMap<>();

  @Override
  public Mono<Boolean> createRepository(Repository repository) {
    return Mono.fromRunnable(() -> {
      if (repositoryExists(repository)) {
        throw new DuplicateRepositoryException(repository.toString());
      }

      map.putIfAbsent(repository.namespace(), new HashMap<>());
      map.get(repository.namespace()).put(repository.name(), new HashMap<>());
    }).then(Mono.just(true));
  }


  @Override
  public Mono<Document> get(RepositoryEntryKey key) {
    return Mono.fromCallable(() -> getRepository(key.repository()))
        .filter(repository -> repository.containsKey(key.key()))
        .switchIfEmpty(Mono.error(new KeyNotFoundException(key.toString())))
        .map(repository -> repository.get(key.key()));
  }


  @Override
  public Mono<Document> put(RepositoryEntryKey key, Document value) {
    return Mono.fromCallable(() -> getRepository(key.repository()))
        .map(repository -> {
          repository.put(key.key(), value);
          return value;
        });
  }

  @Override
  public Mono<String> remove(RepositoryEntryKey key) {
    return Mono.fromCallable(() -> getRepository(key.repository()))
        .filter(repository -> repository.containsKey(key.key()))
        .switchIfEmpty(Mono.error(new KeyNotFoundException(key.toString())))
        .map(repository -> repository.remove(key.key()))
        .thenReturn(key.key());
  }

  @Override
  public Flux<Document> entries(Repository repository) {
    return Flux.fromIterable(getRepository(repository).values());
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
