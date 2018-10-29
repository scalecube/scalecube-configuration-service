package io.scalecube.configuration.repository;

import java.util.Collection;

/**
 * An abstraction of configuration data access.
 */
public interface ConfigurationDataAccess {

  /**
   * Creates a name in underlying data source.
   * @param repository Repository settings
   * @return True if name created; false otherwise.
   */
  boolean createRepository(Repository repository);

  /**
   * Returns a document corresponding to the <code>key</code> argument.
   * @param key Document key
   * @return Document
   */
  Document get(RepositoryEntryKey key);

  /**
   * Puts a the <code>document</code> argument with the corresponding <code>key</code> argument in
   * the underlying data source.
   * @param key Document key
   * @param document The document to upsert in the underlying data source.
   * @return The upserted document
   */
  Document put(RepositoryEntryKey key, Document document);

  /**
   * Removes a document corresponding to the <code>key</code> argument from the underlying
   *     data source.
   * @param key Document key
   * @return The key of the removed document
   */
  String remove(RepositoryEntryKey key);

  /**
   * Returns all the entries in the name.
   * @param repository Repository info
   * @return Collection of documents
   */
  Collection<Document> entries(Repository repository);
}
