package io.scalecube.configuration.repository.couchbase.operation;

import static com.couchbase.client.java.query.Select.select;
import static com.couchbase.client.java.query.dsl.Expression.i;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.SimpleN1qlQuery;
import io.scalecube.configuration.repository.Document;
import io.scalecube.configuration.repository.exception.DataRetrievalFailureException;
import io.scalecube.configuration.repository.exception.OperationInterruptedException;
import io.scalecube.configuration.repository.exception.QueryTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import rx.Observable;

final class ListEntriesOperation extends EntryOperation {

  protected ListEntriesOperation() {
  }

  @Override
  public List<Document> execute(OperationContext context) {
    return entries(context);
  }

  private List<Document> entries(OperationContext context) {
    Objects.requireNonNull(context.repository());
    logger.debug("enter: entries -> repository = [ {} ]", context.repository());
    List<Document> entries = new ArrayList<>();

    try {
      final Bucket bucket = openBucket(context);
      final SimpleN1qlQuery query = N1qlQuery.simple(select("*").from(i(bucket.name())));
      entries =
          executeAsync(bucket.async().query(query))
              .flatMap(
                  result ->
                      result
                          .rows()
                          .mergeWith(
                              result
                                  .errors()
                                  .flatMap(
                                      error ->
                                          Observable.error(
                                              new DataRetrievalFailureException(
                                                  "N1QL error: " + error.toString()))))
                          .flatMap(
                              row ->
                                  Observable.just(
                                      decode(row.value().get(bucket.name()).toString())))
                          .toList())
              .toBlocking()
              .single();
    } catch (Throwable throwable) {
      String message = String.format("Failed to get entries from repository: '%s'",
          context.repository());
      handleException(throwable, message);
    }
    logger.debug(
        "exit: entries -> repository = [ {} ], return = [ {} ] entries",
        context.repository(),
        entries.size());
    return entries;
  }

  private <R> Observable<R> executeAsync(Observable<R> asyncAction) {
    return asyncAction.onErrorResumeNext(
        ex -> {
          if (ex instanceof RuntimeException) {
            return Observable.error(
                translateExceptionIfPossible((RuntimeException) ex));
          } else if (ex instanceof TimeoutException) {
            return Observable.error(new QueryTimeoutException(ex.getMessage(), ex));
          } else if (ex instanceof InterruptedException) {
            return Observable.error(new OperationInterruptedException(ex.getMessage(), ex));
          } else if (ex instanceof ExecutionException) {
            return Observable.error(new OperationInterruptedException(ex.getMessage(), ex));
          } else {
            return Observable.error(ex);
          }
        });
  }

}
