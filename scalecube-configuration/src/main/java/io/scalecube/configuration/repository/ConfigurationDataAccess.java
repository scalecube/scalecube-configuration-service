package io.scalecube.configuration.repository;

import java.util.Collection;

/**
 * An abstraction of configuration data access.
 */
public interface ConfigurationDataAccess {

  /**
   * Creates a repository in underlying data source.
   * @param namespace Repository namespace
   * @param repository Repository name
   * @return True if repository created; false otherwise.
   */
  boolean createRepository(String namespace, String repository);

  /**
   * Returns a document corresponding to the <code>key</code> argument.
   * @param namespace Repository namespace
   * @param repository Repository name
   * @param key Document key
   * @return Document
   */
  Document get(String namespace, String repository, String key);

  /**
   * Puts a the <code>document</code> argument with the corresponding <code>key</code> argument in
   * the underlying data source.
   * @param namespace Repository namespace
   * @param repository Repository name
   * @param key Document key
   * @param document The document to upsert in the underlying data source.
   * @return The upserted document
   */
  Document put(String namespace, String repository, String key, Document document);

  /**
   * Removes a document corresponding to the <code>key</code> argument from the underlying
   *     data source.
   * @param namespace Repository namespace
   * @param repository Repository name
   * @param key Document key
   * @return The key of the removed document
   */
  String remove(String namespace, String repository, String key);

  /**
   * Returns all the entries in the repository.
   * @param namespace Repository namespace
   * @param repository Repository name
   * @return Collection of documents
   */
  Collection<Document> entries(String namespace, String repository);
}
