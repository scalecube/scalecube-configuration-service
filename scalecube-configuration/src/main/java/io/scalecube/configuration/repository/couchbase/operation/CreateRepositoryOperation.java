package io.scalecube.configuration.repository.couchbase.operation;

import io.scalecube.configuration.repository.couchbase.ConfigurationBucketName;
import io.scalecube.configuration.repository.couchbase.CouchbaseAdmin;
import io.scalecube.configuration.repository.couchbase.CouchbaseExceptionTranslator;
import io.scalecube.configuration.repository.exception.DataAccessException;
import io.scalecube.configuration.repository.exception.DataAccessResourceFailureException;
import io.scalecube.configuration.repository.exception.DuplicateRepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class CreateRepositoryOperation {

  private static Logger logger = LoggerFactory.getLogger(CreateRepositoryOperation.class);
  private final CouchbaseExceptionTranslator exceptionTranslator;

  public CreateRepositoryOperation() {
    exceptionTranslator = new CouchbaseExceptionTranslator();
  }

  /**
   * Creates a repository using the given arguments.
   *
   * @param couchbaseAdmin Couchbase admin API.
   * @param ctx operation context
   * @return true if the operation completes successfully
   */
  public Mono<Void> execute(CouchbaseAdmin couchbaseAdmin, OperationContext ctx) {
    logger.debug(
        "enter: createBucket -> repository = [ {} ]", ctx.repository());
    return Mono.create(sink -> {
      try {
        String bucketName = ConfigurationBucketName
            .from(ctx.repository(), ctx.settings()).name();
        ensureBucketNameIsNotInUse(couchbaseAdmin, bucketName);
        couchbaseAdmin.createBucket(bucketName);
        logger.debug("exit: createBucket -> repository = [ {} ]", ctx.repository());
        sink.success();
      } catch (Throwable ex) {
        String message = String.format("Failed to create repository: '%s'", ctx.repository());
        sink.error(handleException(ex, message));
      }
    });
  }

  private void ensureBucketNameIsNotInUse(CouchbaseAdmin couchbaseAdmin, String name) {
    if (couchbaseAdmin.isBucketExists(name)) {
      throw new DuplicateRepositoryException("Repository with name: '" + name + " already exists.");
    }
  }

  private Throwable handleException(Throwable throwable, String message) {
    logger.error(message, throwable);
    if (throwable instanceof DataAccessException) {
      return throwable;
    } else if (throwable instanceof RuntimeException) {
      return exceptionTranslator.translateExceptionIfPossible((RuntimeException) throwable);
    }
    return new DataAccessResourceFailureException(message, throwable);
  }
}
