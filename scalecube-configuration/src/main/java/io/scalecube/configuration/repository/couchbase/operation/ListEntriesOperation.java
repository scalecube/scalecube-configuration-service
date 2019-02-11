package io.scalecube.configuration.repository.couchbase.operation;

import static com.couchbase.client.java.query.Select.select;
import static com.couchbase.client.java.query.dsl.Expression.i;

import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.SimpleN1qlQuery;
import io.scalecube.configuration.repository.Document;
import io.scalecube.configuration.repository.couchbase.CouchbaseExceptionTranslator;
import io.scalecube.configuration.repository.exception.DataRetrievalFailureException;
import java.util.Objects;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rx.Observable;
import rx.RxReactiveStreams;

final class ListEntriesOperation extends EntryOperation<Flux<Document>> {

  @Override
  public Flux<Document> execute(OperationContext context) {
    return entries(context);
  }

  private Flux<Document> entries(OperationContext context) {
    return Mono.fromRunnable(
        () -> {
          logger.debug("enter: entries -> repository = [ {} ]", context.repository());
          Objects.requireNonNull(context.repository());
        })
        .then(openBucket(context))
        .flatMapMany(
            bucket -> {
              String bucketName = bucket.name();
              SimpleN1qlQuery query = N1qlQuery.simple(select("*").from(i(bucketName)));

              return Flux.from(RxReactiveStreams.toPublisher(query(bucket, query)));
            })
        .onErrorMap(CouchbaseExceptionTranslator::translateExceptionIfPossible)
        .doOnError(
            th -> logger.error("Failed to get entries from repository: {}", context.repository()))
        .doOnComplete(
            () -> logger.debug("exit: entries -> repository = [ {} ]", context.repository()));
  }

  private Observable<Document> query(AsyncBucket bucket, SimpleN1qlQuery query) {
    return bucket
        .query(query)
        .flatMap(
            queryResult ->
                queryResult
                    .rows()
                    .mergeWith(
                        queryResult
                            .errors()
                            .flatMap(
                                error ->
                                    Observable.error(
                                        new DataRetrievalFailureException(
                                            "N1QL error: " + error.toString()))))
                    .map(row -> decode(row.value().get(bucket.name()).toString())));
  }
}
