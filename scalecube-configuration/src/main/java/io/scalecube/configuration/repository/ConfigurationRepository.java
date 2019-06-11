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
   * @return Document instance by a given key.
   */
  Mono<Document> readEntry(String tenant, String repository, String key);

  /**
   * Fetch all keys from a tenant repository.
   *
   * @param tenant namespace of the repository.
   * @param repository to fetch from.
   * @return stream of Document instances in the repository.
   */
  Flux<Document> readList(String tenant, String repository);

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
   * Delete a key from a tenant repository by key.
   *
   * @param tenant namespace of the repository.
   * @param repository to delete from.
   * @param key of the document to delete.
   */
  Mono<Void> deleteEntry(String tenant, String repository, String key);
}
