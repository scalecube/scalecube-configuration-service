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
  public Mono<Document> readEntry(String tenant, String repository, String key) {
    return get(new Repository(tenant, repository), key);
  }

  @Override
  public Flux<Document> fetchAll(String tenant, String repository) {
    return entries(new Repository(tenant, repository));
  }

  @Override
  public Mono<Document> save(String tenant, String repository, Document document) {
    return put(new Repository(tenant, repository), document.key(), document);
  }

  @Override
  public Mono<Void> delete(String tenant, String repository, String key) {
    return remove(new Repository(tenant, repository), key);
  }

  private Mono<Document> get(Repository repository, String key) {
    return Mono.justOrEmpty(getRepository(repository).get(key))
        .switchIfEmpty(
            Mono.defer(
                () ->
                    Mono.error(
                        new KeyNotFoundException(String.format("Key '%s' not found", key)))));
  }

  private Mono<Document> put(Repository repository, String key, Document value) {
    return Mono.create(sink -> sink.success(getRepository(repository).put(key, value)));
  }

  private Mono<Void> remove(Repository repository, String key) {
    return Mono.fromCallable(() -> getRepository(repository))
        .filter(repo -> repo.containsKey(key))
        .switchIfEmpty(
            Mono.defer(
                () ->
                    Mono.error(new KeyNotFoundException(String.format("Key '%s' not found", key)))))
        .map(repo -> repo.remove(key))
        .then();
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
