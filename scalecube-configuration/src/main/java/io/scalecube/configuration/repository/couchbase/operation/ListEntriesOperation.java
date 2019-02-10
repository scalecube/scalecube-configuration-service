package io.scalecube.configuration.repository.couchbase.operation;

import static com.couchbase.client.java.query.Select.select;
import static com.couchbase.client.java.query.dsl.Expression.i;
import static rx.Observable.error;

import com.couchbase.client.java.query.AsyncN1qlQueryResult;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.SimpleN1qlQuery;
import io.scalecube.configuration.repository.Document;
import io.scalecube.configuration.repository.exception.DataRetrievalFailureException;
import io.scalecube.configuration.repository.exception.OperationInterruptedException;
import io.scalecube.configuration.repository.exception.QueryTimeoutException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import reactor.core.publisher.Flux;
import rx.Observable;
import rx.functions.Func1;

final class ListEntriesOperation extends EntryOperation {

  private static final String ALL = "*";
  private final Func1<Throwable, Observable<? extends AsyncN1qlQueryResult>> errorMapper = errorMapper();

  ListEntriesOperation() {
  }

  @Override
  public Flux<Document> execute(OperationContext context) {
    return Flux.from(sink -> {
      Objects.requireNonNull(context.repository(), "context.repository is null");
      logger.debug("enter: entries -> repository = [ {} ]", context.repository());

      Observable<Document> source = asyncBucket(context)
          .flatMap(bucket -> {
            SimpleN1qlQuery query = N1qlQuery.simple(select(ALL).from(i(bucket.name())));
            return bucket.query(query)
                .onErrorResumeNext(errorMapper)
                .flatMap(queryResult -> {
                      queryResult.finalSuccess().doOnNext(finalSuccess -> sink.onComplete());
                      return queryResult
                          .rows()
                          .mergeWith(queryResult
                              .errors()
                              .flatMap(errObj -> error(new DataRetrievalFailureException(
                                  "N1QL error: " + errObj.toString()))))
                          .map(row -> decode(row.value().get(bucket.name()).toString()));
                    }
                );
          });
      source
          .doOnError(sink::onError)
          .doOnNext(sink::onNext)
          .doOnCompleted(sink::onComplete);
    });
  }

  private Func1<Throwable, Observable<? extends AsyncN1qlQueryResult>> errorMapper() {
    return ex -> {
      if (ex instanceof RuntimeException) {
        return error(
            translateExceptionIfPossible((RuntimeException) ex));
      } else if (ex instanceof TimeoutException) {
        return error(new QueryTimeoutException(ex.getMessage(), ex));
      } else if (ex instanceof InterruptedException) {
        return error(new OperationInterruptedException(ex.getMessage(), ex));
      } else if (ex instanceof ExecutionException) {
        return error(new OperationInterruptedException(ex.getMessage(), ex));
      } else {
        return error(ex);
      }
    };
  }
}
