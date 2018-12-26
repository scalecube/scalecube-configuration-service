package io.scalecube.configuration.repository.couchbase;

import com.couchbase.client.core.BackpressureException;
import com.couchbase.client.core.BucketClosedException;
import com.couchbase.client.core.DocumentConcurrentlyModifiedException;
import com.couchbase.client.core.ReplicaNotConfiguredException;
import com.couchbase.client.core.RequestCancelledException;
import com.couchbase.client.core.ServiceNotAvailableException;
import com.couchbase.client.core.config.ConfigurationException;
import com.couchbase.client.core.endpoint.SSLException;
import com.couchbase.client.core.endpoint.kv.AuthenticationException;
import com.couchbase.client.core.env.EnvironmentException;
import com.couchbase.client.core.state.NotConnectedException;
import com.couchbase.client.java.error.BucketDoesNotExistException;
import com.couchbase.client.java.error.CASMismatchException;
import com.couchbase.client.java.error.DesignDocumentException;
import com.couchbase.client.java.error.DocumentAlreadyExistsException;
import com.couchbase.client.java.error.DocumentDoesNotExistException;
import com.couchbase.client.java.error.DurabilityException;
import com.couchbase.client.java.error.InvalidPasswordException;
import com.couchbase.client.java.error.RequestTooBigException;
import com.couchbase.client.java.error.TemporaryFailureException;
import com.couchbase.client.java.error.TemporaryLockFailureException;
import com.couchbase.client.java.error.TranscodingException;
import com.couchbase.client.java.error.ViewDoesNotExistException;

import io.scalecube.configuration.repository.exception.DataAccessException;
import io.scalecube.configuration.repository.exception.DataAccessResourceFailureException;
import io.scalecube.configuration.repository.exception.DataIntegrityViolationException;
import io.scalecube.configuration.repository.exception.DataRetrievalFailureException;
import io.scalecube.configuration.repository.exception.DuplicateKeyException;
import io.scalecube.configuration.repository.exception.InvalidDataAccessResourceUsageException;
import io.scalecube.configuration.repository.exception.KeyNotFoundException;
import io.scalecube.configuration.repository.exception.OperationCancellationException;
import io.scalecube.configuration.repository.exception.QueryTimeoutException;
import io.scalecube.configuration.repository.exception.TransientDataAccessResourceException;

import java.util.concurrent.TimeoutException;

/**
 * Utility class used to translate runtime exception into a more meaningful exceptions.
 */
public final class CouchbaseExceptionTranslator {

  /**
   * Return a translation os the ex argument into a {@link DataAccessException} if possible.
   * @param ex the runtime exception to try and translate
   * @return A {@link DataAccessException} translation of the ex argument if possible;
   *     the ex argument otherwise
   */
  public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
    if (ex instanceof InvalidPasswordException
        || ex instanceof NotConnectedException
        || ex instanceof ConfigurationException
        || ex instanceof EnvironmentException
        || ex instanceof SSLException
        || ex instanceof ServiceNotAvailableException
        || ex instanceof BucketClosedException
        || ex instanceof BucketDoesNotExistException
        || ex instanceof AuthenticationException) {
      return new DataAccessResourceFailureException(ex.getMessage(), ex);
    }

    if (ex instanceof DocumentAlreadyExistsException) {
      return new DuplicateKeyException(ex.getMessage(), ex);
    }

    if (ex instanceof DocumentDoesNotExistException) {
      return new KeyNotFoundException(ex.getMessage(), ex);
    }

    if (ex instanceof CASMismatchException
        || ex instanceof DocumentConcurrentlyModifiedException
        || ex instanceof ReplicaNotConfiguredException
        || ex instanceof DurabilityException) {
      return new DataIntegrityViolationException(ex.getMessage(), ex);
    }

    if (ex instanceof RequestCancelledException
        || ex instanceof BackpressureException) {
      return new OperationCancellationException(ex.getMessage(), ex);
    }

    if (ex instanceof ViewDoesNotExistException
        || ex instanceof RequestTooBigException
        || ex instanceof DesignDocumentException) {
      return new InvalidDataAccessResourceUsageException(ex.getMessage(), ex);
    }

    if (ex instanceof TemporaryLockFailureException
        || ex instanceof TemporaryFailureException) {
      return new TransientDataAccessResourceException(ex.getMessage(), ex);
    }

    if ((ex != null && ex.getCause() instanceof TimeoutException)) {
      return new QueryTimeoutException(ex.getMessage(), ex);
    }

    if (ex instanceof TranscodingException) {
      //note: the more specific CouchbaseQueryExecutionException should be thrown by the template
      //when dealing with TranscodingException in the query/n1ql methods.
      return new DataRetrievalFailureException(ex.getMessage(), ex);
    }

    // Unable to translate exception, therefore just throw the original!
    throw ex;
  }
}
