package io.scalecube.configuration.repository.couchbase;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * A functional callback interface.
 *
 * @param <T> The type returned by this callback.
 */
public interface CouchbaseCallback<T> {

  /**
   * Callback function.
   *
   * @return An instance of this call back type.
   * @throws TimeoutException IN case of a timeout.
   * @throws ExecutionException In case of an execution error.
   * @throws InterruptedException In case of interruption during execution.
   */
  T execute() throws TimeoutException, ExecutionException, InterruptedException;
}
