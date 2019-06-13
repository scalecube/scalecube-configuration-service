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
  Mono<Document> readEntry(String tenant, String repository, String key, Integer version);

  /**
   * Fetch all keys from a tenant repository.
   *
   * @param tenant namespace of the repository.
   * @param repository to fetch from.
   * @return stream of Document instances in the repository.
   */
  Flux<Document> readList(String tenant, String repository, Integer version);

  /**
   * Fetch a key history from a tenant repository by key.
   *
   * @param tenant namespace of the repository.
   * @param repository to fetch from.
   * @param key of the document to fetch history.
   * @return Document instance by a given key.
   */
  Flux<HistoryDocument> readEntryHistory(String tenant, String repository, String key);

  /**
   * Save a key from a tenant repository by key.
   *
   * @param tenant namespace of the repository.
   * @param repository name to save.
   * @param doc the document to save.
   * @return Document instance by a given key.
   */
  Mono<Document> createEntry(String tenant, String repository, Document doc);

  /**
   * Update a key in a tenant repository by key.
   *
   * @param tenant namespace of the repository.
   * @param repository name to update key.
   * @param doc the document to update.
   * @return Document instance by a given key and version.
   */
  Mono<Document> updateEntry(String tenant, String repository, Document doc);

  /**
   * Delete a key from a tenant repository by key.
   *
   * @param tenant namespace of the repository.
   * @param repository to delete from.
   * @param key of the document to delete.
   */
  Mono<Void> deleteEntry(String tenant, String repository, String key);
}
