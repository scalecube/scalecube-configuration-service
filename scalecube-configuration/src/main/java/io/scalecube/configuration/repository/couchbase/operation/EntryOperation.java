package io.scalecube.configuration.repository.couchbase.operation;

import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.Bucket;
import io.scalecube.configuration.repository.Document;
import io.scalecube.configuration.repository.Repository;
import io.scalecube.configuration.repository.couchbase.ConfigurationBucketName;
import io.scalecube.configuration.repository.couchbase.CouchbaseExceptionTranslator;
import io.scalecube.configuration.repository.couchbase.JacksonTranslationService;
import io.scalecube.configuration.repository.couchbase.PasswordGenerator;
import io.scalecube.configuration.repository.couchbase.TranslationService;
import io.scalecube.configuration.repository.exception.DataAccessException;
import io.scalecube.configuration.repository.exception.DataAccessResourceFailureException;
import io.scalecube.configuration.repository.exception.RepositoryNotFoundException;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import rx.Observable;

/**
 * An abstract base class of an entry CRUD operation in a couchbase bucket.
 */
public abstract class EntryOperation {

  protected static Logger logger;
  final TranslationService translationService;
  private final CouchbaseExceptionTranslator exceptionTranslator;

  public enum OperationType {
    Read,
    Write,
    Delete,
    List
  }

  EntryOperation() {
    logger = LoggerFactory.getLogger(getClass());
    translationService = new JacksonTranslationService();
    exceptionTranslator = new CouchbaseExceptionTranslator();
  }

  /**
   * Factory method for constructing an entry operation based on the ty[e argument.
   *
   * @param type operation type
   * @return an instance of {@link EntryOperation} based on the type argument
   */
  public static EntryOperation getOperation(OperationType type) {
    switch (type) {
      case Read:
        return new GetEntryOperation();
      case Write:
        return new PutEntryOperation();
      case Delete:
        return new RemoveEntryOperation();
      case List:
        return new ListEntriesOperation();
      default:
        throw new IllegalStateException();
    }
  }

  /**
   * Execute this {@link EntryOperation} using the {@link OperationContext} argument.
   *
   * @param context the operation context
   * @return a list of document as result of the execution
   */
  public abstract Flux<Document> execute(OperationContext context);


  Bucket openBucket(OperationContext context) {
    Objects.requireNonNull(context, "context");
    String name = getBucketName(context);
    logger.debug("enter: openBucket -> name = [ {} ]", name);
    Bucket bucket;
    try {
      bucket = context.cluster().openBucket(name, PasswordGenerator.md5Hash(name));
    } catch (Throwable throwable) {
      logger.error("Failed to open bucket: '{}', error: {}", name, throwable);
      throw new RepositoryNotFoundException(
          String.format("Failed to open bucket: '%s'", name), throwable);
    }
    logger.debug("exit: openBucket -> name = [ {} ]", bucket.name());
    return bucket;
  }

  Observable<AsyncBucket> asyncBucket(OperationContext context) {
    Objects.requireNonNull(context, "context");
    String name = getBucketName(context);
    logger.debug("enter: openBucket -> name = [{}]", name);
    Observable<AsyncBucket> bucket;
    try {
      bucket = context.cluster().async().openBucket(name, PasswordGenerator.md5Hash(name));
    } catch (Throwable throwable) {
      logger.error("Failed to open bucket: '{}', error: {}", name, throwable);
      throw new RepositoryNotFoundException(
          String.format("Failed to open bucket: '%s'", name), throwable);
    }
    return bucket;
  }

  private static String getBucketName(OperationContext context) {
    if (context.repository() == null && context.key() == null) {
      throw new IllegalStateException("repository is missing");
    }

    Repository repository = context.repository() == null
        ? context.key().repository()
        : context.repository();

    return ConfigurationBucketName.from(repository, context.settings()).name();
  }

  void handleException(Throwable throwable, String message) {
    logger.error(message, throwable);
    if (throwable instanceof DataAccessException) {
      throw (DataAccessException) throwable;
    } else if (throwable instanceof RuntimeException) {
      throw exceptionTranslator.translateExceptionIfPossible((RuntimeException) throwable);
    }
    throw new DataAccessResourceFailureException(message, throwable);
  }

  Document decode(String content) {
    return translationService.decode(content, Document.class);
  }

  String encode(Document document) {
    return translationService.encode(document);
  }

  DataAccessException translateExceptionIfPossible(RuntimeException ex) {
    return exceptionTranslator.translateExceptionIfPossible(ex);
  }
}
