package io.scalecube.configuration.repository.couchbase.operation;

import io.scalecube.configuration.repository.Document;
import io.scalecube.configuration.repository.couchbase.CouchbaseExceptionTranslator;
import java.util.Objects;
import reactor.core.publisher.Mono;

final class GetEntryOperation extends EntryOperation<Mono<Document>> {

  @Override
  public Mono<Document> execute(OperationContext context) {
    return Mono.fromRunnable(
        () -> {
          logger.debug("enter: get -> key = [{}]", context.key());
          Objects.requireNonNull(context.key());
        })
        .then(openBucket(context))
        .flatMap(bucket -> getDocument(bucket, context.key().key()))
        .onErrorMap(CouchbaseExceptionTranslator::translateExceptionIfPossible)
        .doOnError(th -> logger.error("Failed to get key: {}", context.key().key(), th))
        .doOnSuccess(doc -> logger.debug("exit: get key -> [ {} ], return -> [ {} ]", doc.value()));
  }
}
