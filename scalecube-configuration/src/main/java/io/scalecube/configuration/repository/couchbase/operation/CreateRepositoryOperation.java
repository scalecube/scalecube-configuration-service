package io.scalecube.configuration.repository.couchbase.operation;

import io.scalecube.configuration.repository.couchbase.ConfigurationBucketName;
import io.scalecube.configuration.repository.couchbase.CouchbaseAdmin;
import io.scalecube.configuration.repository.couchbase.CouchbaseExceptionTranslator;
import io.scalecube.configuration.repository.exception.DataAccessException;
import io.scalecube.configuration.repository.exception.DataAccessResourceFailureException;
import io.scalecube.configuration.repository.exception.DuplicateRepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateRepositoryOperation {
  private static Logger logger = LoggerFactory.getLogger(CreateRepositoryOperation.class);
  private final CouchbaseExceptionTranslator exceptionTranslator;

  public CreateRepositoryOperation() {
    exceptionTranslator = new CouchbaseExceptionTranslator();
  }

  /**
   * Creates a repository using the given arguments.
   * @param couchbaseAdmin Couchbase admin API.
   * @param context operation context
   * @return true if the operation completes successfully
   */
  public boolean execute(CouchbaseAdmin couchbaseAdmin, OperationContext context) {
    logger.debug(
        "enter: createBucket -> repository = [ {} ]", context.repository());
    try {
      String bucket = ConfigurationBucketName.from(context.repository(), context.settings()).name();
      ensureBucketNameIsNotInUse(couchbaseAdmin, bucket);
      couchbaseAdmin.createBucket(bucket);
    } catch (Throwable ex) {
      String message = String.format("Failed to create repository: '%s'", context.repository());
      handleException(ex, message);
    }

    logger.debug(
        "exit: createBucket -> repository = [ {} ]", context.repository());
    return true;
  }

  private void ensureBucketNameIsNotInUse(CouchbaseAdmin couchbaseAdmin, String name) {
    if (couchbaseAdmin.isBucketExists(name)) {
      throw new DuplicateRepositoryException("Repository with name: '" + name + " already exists.");
    }
  }

  private void handleException(Throwable throwable, String message) {
    logger.error(message, throwable);
    if (throwable instanceof DataAccessException) {
      throw (DataAccessException) throwable;
    } else if (throwable instanceof RuntimeException) {
      throw exceptionTranslator.translateExceptionIfPossible((RuntimeException) throwable);
    }
    throw new DataAccessResourceFailureException(message, throwable);
  }
}
