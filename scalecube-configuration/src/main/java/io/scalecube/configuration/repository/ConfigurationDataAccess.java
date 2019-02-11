package io.scalecube.configuration.repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** An abstraction of configuration data access. */
public interface ConfigurationDataAccess {

  /**
   * Creates a name in underlying data source.
   *
   * @param repository Repository settings
   * @return True if name created; false otherwise.
   */
  Mono<Boolean> createRepository(Repository repository);

  /**
   * Returns a document corresponding to the <code>cluster</code> argument.
   *
   * @param key Document cluster
   * @return Document
   */
  Mono<Document> get(RepositoryEntryKey key);

  /**
   * Puts a the <code>document</code> argument with the corresponding <code>cluster</code> argument
   * in the underlying data source.
   *
   * @param key Document cluster
   * @param document The document to upsert in the underlying data source.
   * @return The upserted document
   */
  Mono<Document> put(RepositoryEntryKey key, Document document);

  /**
   * Removes a document corresponding to the <code>cluster</code> argument from the underlying data
   * source.
   *
   * @param key Document cluster
   * @return The key of the removed document
   */
  Mono<String> remove(RepositoryEntryKey key);

  /**
   * Returns all the entries in the name.
   *
   * @param repository Repository info
   * @return Collection of documents
   */
  Flux<Document> entries(Repository repository);
}
