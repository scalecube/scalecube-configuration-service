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
    return Mono.create(sink->{
      if (!repositoryExists(repository)) {
        map.putIfAbsent(repository.namespace(), new HashMap<>());
        map.get(repository.namespace()).put(repository.name(), new HashMap<>());  
        sink.success(true);
      } else {
        sink.error(new DuplicateRepositoryException(repository.toString()));        
      }
    });
  }

  @Override
  public Mono<Document> fetch(String tenant, String repository, String key) {
    return get(
        RepositoryEntryKey.builder()
            .repository(new Repository(tenant, repository))
            .key(key)
            .build());
  }

  @Override
  public Flux<Document> fetchAll(String tenant, String repository) {
    return entries(new Repository(tenant, repository));
  }

  @Override
  public Mono<Document> save(String tenant, String repository, Document document) {
    return put(
        RepositoryEntryKey.builder().repository(new Repository(tenant, repository)).build(),
        document);
  }

  @Override
  public Mono<String> delete(String tenant, String repository, String key) {
    return remove(
        RepositoryEntryKey.builder().repository(new Repository(tenant, repository)).build());
  }  

  private Mono<Document> get(RepositoryEntryKey key) {
    return Mono.fromCallable(() -> getRepository(key.repository()))
        .filter(repository -> repository.containsKey(key.key()))
        .switchIfEmpty(Mono.defer(() -> Mono.error(new KeyNotFoundException(key.toString()))))
        .map(repository -> repository.get(key.key()));
  }

  private Mono<Document> put(RepositoryEntryKey key, Document value) {
    return Mono.fromCallable(() -> getRepository(key.repository()))
        .map(repository -> {
          repository.put(key.key(), value);
          return value;
        });
  }

  private Mono<String> remove(RepositoryEntryKey key) {
    return Mono.fromCallable(() -> getRepository(key.repository()))
        .filter(repository -> repository.containsKey(key.key()))
        .switchIfEmpty(Mono.defer(() -> Mono.error(new KeyNotFoundException(key.toString()))))
        .map(repository -> repository.remove(key.key()))
        .thenReturn(key.key());
  }

  private Flux<Document> entries(Repository repository) {
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