package io.scalecube.configuration.repository;

import io.scalecube.configuration.repository.exception.KeyVersionNotFoundException;
import io.scalecube.configuration.repository.exception.RepositoryAlreadyExistsException;
import io.scalecube.configuration.repository.exception.RepositoryNotFoundException;
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
    throw new NotImplementedException();
    //    if (version == null) {
    //      return get(new Repository(tenant, repository), key);
    //    }
    //    List values = updates.get(new RepoKeyTuple(tenant + "-" + repository, key));
    //    if (values == null) {
    //      throw new KeyNotFoundException(
    //          String.format("Repository '%s' or its key '%s' not found", repository, key));
    //    } else if (values.size() < version) {
    //      throw new KeyVersionNotFoundException(
    //          String.format("Key '%s' version '%s' not found", key, version));
    //    }
    //    return Mono.justOrEmpty(
    //        new Document(key, ((Document) values.get(version - 1)).value(), version));
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
    throw new NotImplementedException();
    //    return put(new Repository(tenant, repository), document.key(), document);
  }

  @Override
  public Mono<Void> delete(String tenant, String repository, String key) {
    throw new NotImplementedException();
    //    return remove(new Repository(tenant, repository), key);
  }

  @Override
  public Mono<Document> update(String tenant, String repository, Document doc) {
    throw new NotImplementedException();
    //    RepoKeyTuple repoKeyTuple = new RepoKeyTuple(tenant + "-" + repository, doc.key());
    //
    //    List values = updates.get(repoKeyTuple);
    //    if (values == null) {
    //      throw new RepositoryKeyAlreadyExistsException(
    //          String.format("Repository '%s' key '%s' already exists", repository, doc.key()));
    //    } else {
    //      values.add(doc.value());
    //      updates.put(repoKeyTuple, values);
    //    }
    //    return Mono.justOrEmpty(doc);
  }

  //  private Mono<Document> get(Repository repository, String key) {
  //    return Mono.justOrEmpty(getRepositoryKey(repository, key).get(key))
  //        .switchIfEmpty(
  //            Mono.defer(
  //                () ->
  //                    Mono.error(
  //                        new KeyNotFoundException(
  //                            String.format(
  //                                "Repository '%s' or its key '%s' not found",
  //                                repository.name(), key)))));
  //  }
  //
  //  private Mono<Document> put(Repository repository, String key, Document value) {
  //    checkRepoKeyPairUnique(repository, key, value);
  //    return Mono.create(sink -> sink.success(getRepository(repository).put(key, value)));
  //  }
  //
  //  private void checkRepoKeyPairUnique(Repository repository, String key, Document value) {
  //    RepoKeyTuple repoKeyTuple =
  //        new RepoKeyTuple(repository.namespace() + "-" + repository.name(), key);
  //
  //    if (updates.get(repoKeyTuple) != null) {
  //      throw new RepositoryKeyAlreadyExistsException(
  //          String.format("Repository '%s' key '%s' already exists", repository.name(), key));
  //    } else {
  //      updates.put(
  //          repoKeyTuple,
  //          new ArrayList() {
  //            {
  //              add(value);
  //            }
  //          });
  //      getRepository(repository).put(key, value);
  //    }
  //  }
  //
  //  private Mono<Void> remove(Repository repository, String key) {
  //    return Mono.fromCallable(() -> getRepositoryKey(repository, key))
  //        .filter(repo -> repo.containsKey(key))
  //        .switchIfEmpty(
  //            Mono.defer(
  //                () ->
  //                    Mono.error(
  //                        new KeyNotFoundException(
  //                            String.format(
  //                                "Repository '%s' or its key '%s' not found",
  //                                repository.name(), key)))))
  //        .map(repo -> repo.remove(key))
  //        .then();
  //  }

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
