package io.scalecube.configuration.repository.couchbase.operation;

import io.scalecube.configuration.repository.couchbase.ConfigurationBucketName;
import io.scalecube.configuration.repository.couchbase.CouchbaseAdmin;
import io.scalecube.configuration.repository.couchbase.CouchbaseExceptionTranslator;
import io.scalecube.configuration.repository.exception.RepositoryAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class CreateRepositoryOperation {

  private static final Logger logger = LoggerFactory.getLogger(CreateRepositoryOperation.class);

  /**
   * Creates a repository using the given arguments.
   *
   * @param couchbaseAdmin Couchbase admin API.
   * @param ctx operation context
   * @return true if the operation completes successfully
   */
  public Mono<Boolean> execute(CouchbaseAdmin couchbaseAdmin, OperationContext ctx) {
    return Mono.fromRunnable(
        () -> logger.debug("enter: createBucket -> repository = [ {} ]", ctx.repository()))
        .then(
            Mono.fromCallable(
                () -> ConfigurationBucketName.from(ctx.repository(), ctx.settings()).name()))
        .flatMap(
            bucketName ->
                ensureBucketNameIsNotInUse(couchbaseAdmin, bucketName)
                    .then(couchbaseAdmin.createBucket(bucketName)))
        .onErrorMap(CouchbaseExceptionTranslator::translateExceptionIfPossible)
        .doOnError(th -> logger.error("Failed to create repository: {}", ctx.repository(), th))
        .doOnSuccess(
            created -> logger.debug("exit: createBucket -> repository = [ {} ]", ctx.repository()));
  }

  private Mono<Void> ensureBucketNameIsNotInUse(CouchbaseAdmin couchbaseAdmin, String name) {
    return couchbaseAdmin
        .isBucketExists(name)
        .handle(
            (exists, sink) -> {
              if (exists) {
                sink.error(
                    new RepositoryAlreadyExistsException(
                        "Repository with name: '" + name + " already exists."));
              }
            });
  }
}
