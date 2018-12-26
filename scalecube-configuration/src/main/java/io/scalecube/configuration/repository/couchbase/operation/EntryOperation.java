package io.scalecube.configuration.repository.couchbase.operation;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;

import io.scalecube.configuration.repository.Document;
import io.scalecube.configuration.repository.Repository;
import io.scalecube.configuration.repository.couchbase.ConfigurationBucketName;
import io.scalecube.configuration.repository.couchbase.CouchbaseExceptionTranslator;
import io.scalecube.configuration.repository.couchbase.JacksonTranslationService;
import io.scalecube.configuration.repository.couchbase.PasswordGenerator;
import io.scalecube.configuration.repository.couchbase.TranslationService;
import io.scalecube.configuration.repository.exception.DataAccessException;
import io.scalecube.configuration.repository.exception.DataAccessResourceFailureException;
import io.scalecube.configuration.repository.exception.KeyNotFoundException;
import io.scalecube.configuration.repository.exception.RepositoryNotFoundException;

import java.util.List;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract base class of an entry CRUD operation in a couchbase bucket.
 */
public abstract class EntryOperation {
  protected static Logger logger;
  private final TranslationService translationService;
  private final CouchbaseExceptionTranslator exceptionTranslator;

  public enum OperationType {
    Read,
    Write,
    Delete,
    List
  }

  protected EntryOperation() {
    logger = LoggerFactory.getLogger(getClass());
    translationService = new JacksonTranslationService();
    exceptionTranslator = new CouchbaseExceptionTranslator();
  }

  /**
   * Factory method for constructing an entry operation based on the ty[e argument.
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
   * @param context the operation context
   * @return a list of document as result of the execution
   */
  public abstract List<Document> execute(OperationContext context);

  protected Document getDocument(Bucket bucket, String id) {
    logger.debug("enter: getDocument -> bucket = [ {} ], [ {} ]", bucket.name(), id);

    Document document;
    JsonDocument jsonDocument = bucket.get(id);

    if (jsonDocument == null) {
      throw new KeyNotFoundException(id);
    }

    try {
      document = toEntity(id, bucket.name(), jsonDocument);
    } catch (Throwable throwable) {
      logger.error("Failed to get document with id: '{}' from bucket: '{}'", id, bucket.name());
      String message =
          String.format(
              "Failed to get document with id: '%s' from bucket: '%s'", id, bucket.name());
      if (throwable instanceof DataAccessException) {
        throw throwable;
      }
      throw new DataAccessResourceFailureException(message, throwable);
    }
    logger.debug("exit: getDocument -> bucket = [ {} ], [ {} ]", bucket.name(), document.key());
    return document;
  }

  private Document toEntity(String id, String bucket, JsonDocument document) {
    logger.debug(
        "enter: toEntity -> bucket = [ {} ] id = [ {} ], document = [ {} ]", bucket, id, document);
    Document entity = null;

    try {
      if (document != null) {
        entity = translationService.decode(document.content().toString(), Document.class);
      }
    } catch (Throwable throwable) {
      logger.error(
          "Failed to decode json document bucket = '%s', id = '%s', document = %s",
          bucket, id, document);
      throw new DataAccessResourceFailureException(
          String.format(
              "Failed to Failed to decode json document bucket = '%s', id = '%s'", bucket, id),
          throwable);
    }

    logger.debug(
        "exit: toEntity -> bucket = [ {} ] id = [ {} ], return = [ {} ]", bucket, id, entity);

    return entity;
  }

  protected Bucket openBucket(OperationContext context) {
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

  private static String getBucketName(OperationContext context) {
    if (context.repository() == null && context.key() == null) {
      throw new IllegalStateException("repository is missing");
    }

    Repository repository = context.repository() == null
        ? context.key().repository()
        : context.repository();

    return ConfigurationBucketName.from(repository, context.settings()).name();
  }

  protected void handleException(Throwable throwable, String message) {
    logger.error(message, throwable);
    if (throwable instanceof DataAccessException) {
      throw (DataAccessException) throwable;
    } else if (throwable instanceof RuntimeException) {
      throw exceptionTranslator.translateExceptionIfPossible((RuntimeException) throwable);
    }
    throw new DataAccessResourceFailureException(message, throwable);
  }

  protected Document decode(String content) {
    return translationService.decode(content, Document.class);
  }

  protected String encode(Document document) {
    return translationService.encode(document);
  }

  protected DataAccessException translateExceptionIfPossible(RuntimeException ex) {
    return exceptionTranslator.translateExceptionIfPossible(ex);
  }
}
