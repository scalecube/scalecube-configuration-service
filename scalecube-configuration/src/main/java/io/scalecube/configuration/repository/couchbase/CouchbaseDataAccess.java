package io.scalecube.configuration.repository.couchbase;

import io.scalecube.configuration.repository.ConfigurationDataAccess;
import io.scalecube.configuration.repository.Document;
import io.scalecube.configuration.repository.exception.DataAccessException;
import io.scalecube.configuration.repository.exception.DataAccessResourceFailureException;
import io.scalecube.configuration.repository.exception.DuplicateRepositoryException;
import io.scalecube.configuration.repository.exception.InvalidRepositoryNameException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Collection;
import java.util.Objects;


public class CouchbaseDataAccess extends CouchbaseOperations
    implements ConfigurationDataAccess<Document> {

  private static Logger logger = LoggerFactory.getLogger(CouchbaseDataAccess.class);
  private final CouchbaseAdmin couchbaseAdmin;

  public CouchbaseDataAccess() {
    couchbaseAdmin = new CouchbaseAdmin();
  }

  @Override
  public void createRepository(String namespace, String repository) {
    logger.debug("enter: createRepository -> namespace = [ {} ], repository = [{}]",
        namespace, repository);
    Objects.requireNonNull(namespace, "namespace");
    Objects.requireNonNull(repository, "repository");
    String repositoryName = getRepositoryName(namespace, repository);
    validateNewBucket(repositoryName);

    try {
      couchbaseAdmin.createRepository(repositoryName);
    } catch (Throwable ex) {
      logger.error("Failed to create repository", ex);
      if (ex instanceof DataAccessException) {
        throw ex;
      }
      throw new DataAccessResourceFailureException("Failed to create repository", ex);
    } finally {
      logger.debug("exit: createRepository -> namespace = [ {} ], repository = [{}]",
          namespace, repository);
    }
  }

  @Override
  public Document get(String namespace, String repository, String key) {
    return null;
  }

  @Override
  public Document put(String namespace, String repository, String key, Document doc) {
    return null;
  }

  @Override
  public Document remove(String namespace, String repository, String key) {
    return null;
  }

  @Override
  public Collection<Document> entries(String namespace, String repository) {
    return null;
  }

  private String getRepositoryName(String namespace, String repository) {
    return String.format(settings.bucketNamePattern(), namespace, repository);
  }

  private void validateNewBucket(String name) {
    if (name == null || name.length() == 0) {
      throw new InvalidRepositoryNameException("name be empty");
    }

    if (!name.matches("^[.%a-zA-Z0-9_-]*$")) {
      throw new InvalidRepositoryNameException(
          "name can only contain characters in range A-Z, a-z, 0-9 as well as "
              + "underscore, period, dash & percent.");
    }

    if (couchbaseAdmin.isBucketExists(name)) {
      throw new DuplicateRepositoryException("repository with name: '" + name + " already exists.");
    }
  }
}
