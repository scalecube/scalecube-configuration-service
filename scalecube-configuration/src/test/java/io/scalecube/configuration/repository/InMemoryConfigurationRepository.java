package io.scalecube.configuration.repository;

import io.scalecube.configuration.repository.exception.KeyNotFoundException;
import io.scalecube.configuration.repository.exception.RepositoryAlreadyExistsException;
import io.scalecube.configuration.repository.exception.RepositoryNotFoundException;
import java.util.HashMap;
import java.util.Map;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class InMemoryConfigurationRepository implements ConfigurationRepository {

  private final Map<String, Map<String, Map<String, Document>>> map = new HashMap<>();

  @Override
  public Mono<Boolean> createRepository(Repository repository) {
    return Mono.create(
        sink -> {
          if (!repositoryExists(repository)) {
            map.putIfAbsent(repository.namespace(), new HashMap<>());
            map.get(repository.namespace()).put(repository.name(), new HashMap<>());
            sink.success(true);
          } else {
            sink.error(
                new RepositoryAlreadyExistsException(
                    "Repository with name: '" + repository.name() + "' already exists"));
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
    return put(repository(tenant, repository, document.key()), document);
  }

  private RepositoryEntryKey repository(String tenant, String repository, String key) {
    return RepositoryEntryKey.builder()
        .key(key)
        .repository(new Repository(tenant, repository))
        .build();
  }

  @Override
  public Mono<String> delete(String tenant, String repository, String key) {
    return remove(repository(tenant, repository, key));
  }

  private Mono<Document> get(RepositoryEntryKey key) {
    return Mono.justOrEmpty(getRepository(key.repository()).get(key.key()))
        .switchIfEmpty(
            Mono.defer(
                () ->
                    Mono.error(
                        new KeyNotFoundException(String.format("Key '%s' not found", key.key())))));
  }

  private Mono<Document> put(RepositoryEntryKey key, Document value) {
    return Mono.create(
        sink -> {
          sink.success(getRepository(key.repository()).put(key.key(), value));
        });
  }

  private Mono<String> remove(RepositoryEntryKey key) {
    return Mono.fromCallable(() -> getRepository(key.repository()))
        .filter(repository -> repository.containsKey(key.key()))
        .switchIfEmpty(
            Mono.defer(
                () ->
                    Mono.error(
                        new KeyNotFoundException(String.format("Key '%s' not found", key.key())))))
        .map(repository -> repository.remove(key.key()))
        .thenReturn(key.key());
  }

  private Flux<Document> entries(Repository repository) {
    return Flux.fromIterable(getRepository(repository).values());
  }

  private Map<String, Document> getRepository(Repository repository) {
    if (repositoryExists(repository)) {
      return map.get(repository.namespace()).get(repository.name());
    } else {
      throw new RepositoryNotFoundException(
          String.format(
              "Repository '%s' not found", repository.namespace() + "-" + repository.name()));
    }
  }

  private boolean repositoryExists(Repository repository) {
    return map.containsKey(repository.namespace())
        && map.get(repository.namespace()).containsKey(repository.name());
  }
}
