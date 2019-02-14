package io.scalecube.configuration.repository.couchbase.admin;

import com.couchbase.client.java.AsyncBucket;
import io.scalecube.configuration.repository.exception.CreatePrimaryIndexException;
import reactor.core.publisher.Mono;
import rx.RxReactiveStreams;

final class CreatePrimaryIndexOperation extends Operation<Mono<Boolean>> {

  @Override
  public Mono<Boolean> execute(AdminOperationContext context) {
    return Mono.from(
            RxReactiveStreams.toPublisher(
                context
                    .cluster()
                    .openBucket(context.name())
                    .flatMap(AsyncBucket::bucketManager)
                    .flatMap(bucketManager -> bucketManager.createN1qlPrimaryIndex(true, false))))
        .onErrorMap(throwable -> new CreatePrimaryIndexException(throwable.getMessage()));
  }
}
