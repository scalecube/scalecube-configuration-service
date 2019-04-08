package io.scalecube.configuration.repository.couchbase.operation;

import io.scalecube.configuration.repository.couchbase.CouchbaseAdmin;
import io.scalecube.configuration.repository.couchbase.CouchbaseExceptionTranslator;
import io.scalecube.configuration.repository.exception.RepositoryAlreadyExistsException;
import io.scalecube.configuration.repository.exception.RepositoryNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class CreateRepositoryOperation {

  private static final Logger logger = LoggerFactory.getLogger(CreateRepositoryOperation.class);

  private static final String CONFIG_BUCKET_NAME = "configurations";

  /**
   * Creates a repository using the given arguments.
   *
   * @param couchbaseAdmin Couchbase admin API.
   * @param ctx operation context
   * @return true if the operation completes successfully
   */
  public Mono<Boolean> execute(CouchbaseAdmin couchbaseAdmin, OperationContext ctx) {
    return ensureBucketNameIsInUse(couchbaseAdmin, CONFIG_BUCKET_NAME)
        .then(
            couchbaseAdmin.insertDoc(CONFIG_BUCKET_NAME, ctx.repository().namespace(),
                ctx.repository().name())
        )
        .onErrorMap(CouchbaseExceptionTranslator::translateExceptionIfPossible)
        .doOnError(th -> logger.error("Failed to create repository: {}", ctx.repository(), th))
        .doOnSuccess(
            created -> logger
                .debug("exit: createBucket -> repository = [ {} ]", ctx.repository()));
  }

  private Mono<Void> ensureBucketNameIsNotInUse(CouchbaseAdmin couchbaseAdmin, String name) {
    return couchbaseAdmin
        .isBucketExists(name)
        .handle(
            (exists, sink) -> {
              if (exists) {
                sink.error(
                    new RepositoryAlreadyExistsException(
                        "Repository with name: '" + name + "' already exists"));
              }
            });
  }

  private Mono<Void> ensureBucketNameIsInUse(CouchbaseAdmin couchbaseAdmin, String name) {
    return couchbaseAdmin
        .isBucketExists(name)
        .handle(
            (exists, sink) -> {
              if (!exists) {
                sink.error(
                    new RepositoryNotFoundException(
                        "Repository with name: '" + name + "' isn't found"));
              }
            });
  }
}
