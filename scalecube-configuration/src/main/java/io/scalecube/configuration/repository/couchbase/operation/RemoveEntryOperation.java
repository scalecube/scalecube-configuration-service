package io.scalecube.configuration.repository.couchbase.operation;

import com.couchbase.client.java.error.DocumentDoesNotExistException;
import io.scalecube.configuration.repository.Document;
import io.scalecube.configuration.repository.couchbase.CouchbaseExceptionTranslator;
import io.scalecube.configuration.repository.exception.KeyNotFoundException;
import java.util.Objects;
import reactor.core.publisher.Mono;
import rx.RxReactiveStreams;

final class RemoveEntryOperation extends EntryOperation<Mono<Document>> {

  @Override
  public Mono<Document> execute(OperationContext context) {
    return Mono.fromRunnable(
        () -> {
          logger.debug("enter: remove -> key = [{}]", context.key());
          Objects.requireNonNull(context.key());
        })
        .then(openBucket(context))
        .flatMap(
            bucket -> Mono.from(RxReactiveStreams.toPublisher(bucket.remove(context.key().key()))))
        .map(jsonDoc -> Document.builder().id(jsonDoc.id()).key(context.key().key()).build())
        // TODO: refactor error handling
        .onErrorMap(
            DocumentDoesNotExistException.class,
            e -> new KeyNotFoundException(String.format("Key '%s' not found", context.key().key())))
        .onErrorMap(CouchbaseExceptionTranslator::translateExceptionIfPossible)
        .doOnError(th -> logger.error("Failed to remove key: {}", context.key().key(), th))
        .doOnSuccess(doc -> logger.debug("exit: remove -> key = [{}]", context.key().key()));
  }
}
