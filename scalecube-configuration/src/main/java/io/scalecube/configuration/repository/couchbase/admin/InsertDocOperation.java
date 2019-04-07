package io.scalecube.configuration.repository.couchbase.admin;

import com.couchbase.client.java.document.RawJsonDocument;
import reactor.core.publisher.Mono;
import rx.Observable;
import rx.RxReactiveStreams;

final class InsertDocOperation extends Operation<Mono<Boolean>> {

  @Override
  public Mono<Boolean> execute(AdminOperationContext context) {
    return Mono.from(
        RxReactiveStreams.toPublisher(
            context
                .cluster()
                .openBucket(context.name())
                .flatMap(asyncBucket -> asyncBucket
                    .upsert(RawJsonDocument.create(context.docName(),
                        "{\"repo_id\":\"" + context.name() + "\"}")))
                .flatMap(rawJsonDocument -> Observable.just(rawJsonDocument.id() != null))
        ))
        .onErrorMap(throwable -> new Exception(throwable.getMessage()));
  }
}
