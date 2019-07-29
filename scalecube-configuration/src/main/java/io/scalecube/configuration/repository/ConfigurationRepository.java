package io.scalecube.configuration.repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** An abstraction of configuration data access. */
public interface ConfigurationRepository {

  /**
   * Creates a repository in underlying data source.
   *
   * @param repository repository settings
   * @return true if repository created; false otherwise.
   */
  Mono<Boolean> createRepository(Repository repository);

  /**
   * Fetch a key from a tenant repository by key.
   *
   * @param tenant namespace of the repository.
   * @param repository to fetch from.
   * @param key of the document to fetch.
   * @param version of the document key to fetch.
   * @return Document instance by a given key.
   */
  Mono<Document> read(String tenant, String repository, String key, Integer version);

  /**
   * Fetch all keys from a tenant repository.
   *
   * @param tenant namespace of the repository.
   * @param repository to fetch from.
   * @return stream of Document instances in the repository.
   */
  Flux<Document> readAll(String tenant, String repository, Integer version);

  /**
   * Fetch a key history (all its version) from a tenant repository by key.
   *
   * @param tenant namespace of the repository.
   * @param repository to fetch from.
   * @param key of the document to fetch history.
   * @return Document instance by a given key.
   */
  Flux<HistoryDocument> readHistory(String tenant, String repository, String key);

  /**
   * Save a key from a tenant repository by key.
   *
   * @param tenant namespace of the repository.
   * @param repository name to save.
   * @param doc the document to save.
   * @return Document instance by a given key.
   */
  Mono<Document> save(String tenant, String repository, Document doc);

  /**
   * Update a key from a tenant repository by key.
   *
   * @param tenant namespace of the repository.
   * @param repository name to update.
   * @param doc the document to update.
   * @return Document instance by a given key.
   */
  Mono<Document> update(String tenant, String repository, Document doc);

  /**
   * Delete a key from a tenant repository by key.
   *
   * @param tenant namespace of the repository.
   * @param repository to delete from.
   * @param key of the document to delete.
   */
  Mono<Void> delete(String tenant, String repository, String key);
}
