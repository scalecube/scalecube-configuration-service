package io.scalecube.configuration.repository;

import io.scalecube.configuration.repository.exception.KeyNotFoundException;
import io.scalecube.configuration.repository.exception.KeyVersionNotFoundException;
import io.scalecube.configuration.repository.exception.RepositoryAlreadyExistsException;
import io.scalecube.configuration.repository.exception.RepositoryKeyAlreadyExistsException;
import io.scalecube.configuration.repository.exception.RepositoryNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class InMemoryConfigurationRepository implements ConfigurationRepository {

  private static final String TENANT_REPO_DELIMITER = "-";

  private final Map<String, Map<String, List<Document>>> repoKeyValues = new HashMap<>();

  @Override
  public Mono<Boolean> createRepository(Repository repository) {
    return Mono.create(
        sink -> {
          if (!repositoryExists(repository)) {
            repoKeyValues.put(tenantRepo(repository), new HashMap<>());
            sink.success(true);
          } else {
            sink.error(
                new RepositoryAlreadyExistsException(
                    "Repository with name: '" + repository.name() + "' already exists"));
          }
        });
  }

  @Override
  public Mono<Document> read(String tenant, String repository, String key, Integer version) {
    return Mono.justOrEmpty(getRepositoryKey(new Repository(tenant, repository), key, version));
  }

  @Override
  public Flux<Document> readAll(String tenant, String repository, Integer version) {
    throw new NotImplementedException();
    //    return entries(new Repository(tenant, repository));
  }

  @Override
  public Flux<HistoryDocument> readHistory(String tenant, String repository, String key) {
    throw new NotImplementedException();
    //    throw new NotImplementedException();
  }

  @Override
  public Mono<Document> save(String tenant, String repository, Document document) {
    return create(new Repository(tenant, repository), document.key(), document);
  }

  @Override
  public Mono<Void> delete(String tenant, String repository, String key) {
    return remove(new Repository(tenant, repository), key);
  }

  @Override
  public Mono<Document> update(String tenant, String repository, Document document) {
    return updating(new Repository(tenant, repository), document.key(), document);
  }

  private Mono<Void> remove(Repository repository, String key) {
    if (repositoryAndKeyExists(repository, key)) {
      return Mono.justOrEmpty(getRepository(repository).remove(key)).then();
    }
    return Mono.defer(
        () ->
            Mono.error(
                new KeyNotFoundException(
                    String.format(
                        "Repository '%s' or its key '%s' not found", repository.name(), key))));
  }

  private Mono<Document> updating(Repository repository, String key, Document value) {
    if (repositoryAndKeyExists(repository, key)) {
      getRepositoryKeyAllVersions(repository, key).add(value);
      return Mono.create(sink -> sink.success(value));
    }
    return Mono.defer(
        () ->
            Mono.error(
                new KeyNotFoundException(
                    String.format(
                        "Repository '%s' or its key '%s' not found", repository.name(), key))));
  }

  private Mono<Document> create(Repository repository, String key, Document value) {
    getRepository(repository);
    if (!repositoryAndKeyExists(repository, key)) {
      getRepository(repository)
          .put(
              key,
              new ArrayList<Document>() {
                {
                  add(value);
                }
              });
      return Mono.create(sink -> sink.success(value));
    }
    throw new RepositoryKeyAlreadyExistsException(
        String.format("Repository '%s' key '%s' already exists", repository.name(), key));
  }

  private Flux<Document> entries(Repository repository) {
    return entries(repository, null);
  }

  private Flux<Document> entries(Repository repository, Integer version) {
    Set<Document> values =
        getRepository(repository).values().stream()
            .map(
                vl ->
                    version == null
                        ? vl.get(vl.size() - 1)
                        : version <= vl.size() ? vl.get(version - 1) : null)
            .filter(v -> v != null)
            .collect(Collectors.toSet());
    return Flux.fromIterable(values);
  }

  private Document getRepositoryKey(Repository repository, String key) {
    return getRepositoryKey(repository, key, null);
  }

  private Document getRepositoryKey(Repository repository, String key, Integer version) {
    List<Document> values = getRepositoryKeyAllVersions(repository, key);
    if (version == null) {
      return values.get(values.size() - 1);
    }
    if (values.size() >= version) {
      return values.get(version - 1);
    }
    throw new KeyVersionNotFoundException(
        String.format("Key '%s' version '%s' not found", key, version));
  }

  private List<Document> getRepositoryKeyAllVersions(Repository repository, String key) {
    List<Document> values;
    if (repositoryExists(repository) && (values = getRepository(repository).get(key)) != null) {
      return values;
    } else {
      throw new RepositoryNotFoundException(
          String.format("Repository '%s' or its key '%s' not found", repository.name(), key));
    }
  }

  private Map<String, List<Document>> getRepository(Repository repository) {
    if (repositoryExists(repository)) {
      return repoKeyValues.get(tenantRepo(repository));
    } else {
      throw new RepositoryNotFoundException(
          String.format("Repository '%s' not found", repository.name()));
    }
  }

  private boolean repositoryAndKeyExists(Repository repository, String key) {
    return repositoryExists(repository) ? getRepository(repository).containsKey(key) : false;
  }

  private boolean repositoryExists(Repository repository) {
    return repoKeyValues.containsKey(tenantRepo(repository));
  }

  private String tenantRepo(Repository repository) {
    return tenantRepo(repository.namespace(), repository.name());
  }

  private String tenantRepo(String tenant, String repoName) {
    return tenant + TENANT_REPO_DELIMITER + repoName;
  }
}
