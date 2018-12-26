package io.scalecube.configuration.repository.couchbase;

import static java.util.Objects.requireNonNull;

import io.scalecube.configuration.repository.exception.OperationInterruptedException;
import io.scalecube.configuration.repository.exception.QueryTimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public abstract class CouchbaseOperations {

  protected final CouchbaseSettings settings;
  protected final CouchbaseExceptionTranslator exceptionTranslator;

  protected CouchbaseOperations(CouchbaseSettings settings) {
    this.settings = settings;
    this.exceptionTranslator = new CouchbaseExceptionTranslator();
  }

  protected final <R> R execute(CouchbaseCallback<R> action) {
    requireNonNull(action);

    try {
      return action.execute();
    } catch (RuntimeException ex) {
      throw exceptionTranslator.translateExceptionIfPossible(ex);
    } catch (TimeoutException ex) {
      throw new QueryTimeoutException(ex.getMessage(), ex);
    } catch (InterruptedException | ExecutionException ex) {
      throw new OperationInterruptedException(ex.getMessage(), ex);
    }
  }
}
