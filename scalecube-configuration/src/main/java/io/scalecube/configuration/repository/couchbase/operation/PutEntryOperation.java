package io.scalecube.configuration.repository.couchbase.operation;

import com.couchbase.client.java.document.RawJsonDocument;
import io.scalecube.configuration.repository.Document;
import io.scalecube.configuration.repository.couchbase.CouchbaseExceptionTranslator;
import java.util.Objects;
import reactor.core.publisher.Mono;
import rx.RxReactiveStreams;

final class PutEntryOperation extends EntryOperation<Mono<Document>> {

  @Override
  public Mono<Document> execute(OperationContext context) {
    return Mono.fromRunnable(
        () -> {
          logger.debug(
              "enter: put -> key = [{}], document = [{}]", context.key(), context.document());

          Objects.requireNonNull(context.key());
          Objects.requireNonNull(context.document());
        })
        .then(openBucket(context))
        .flatMap(
            bucket ->
                Mono.from(
                    RxReactiveStreams.toPublisher(
                        bucket.upsert(
                            RawJsonDocument.create(
                                context.key().key(), encode(context.document()))))))
        .thenReturn(context.document())
        .onErrorMap(CouchbaseExceptionTranslator::translateExceptionIfPossible)
        .doOnError(th -> logger.error("Failed to put key: {}", context.key().key()))
        .doOnSuccess(doc -> logger.debug("exit: put -> key = [{}], document = [{}]", doc));
  }
}
