package io.scalecube.configuration.repository.couchbase.operation;

import io.scalecube.configuration.repository.Document;
import java.util.Objects;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

final class GetEntryOperation extends EntryOperation {

  GetEntryOperation() {
  }

  @Override
  public Flux<Document> execute(OperationContext context) {
    return Flux.from(get(context));
  }

  private Mono<Document> get(OperationContext context) {
    return Mono.create(sink -> {
      Objects.requireNonNull(context.key(), "context.key is null");
      logger.debug("GET for key=[{}]", context.key());
      asyncBucket(context)
          .flatMap(bucket -> bucket.get(context.key().key()))
          .doOnError(sink::error)
          .map(json -> translationService.decode(json.content().toString(), Document.class))
          .doOnError(sink::error);
    });
  }
}
