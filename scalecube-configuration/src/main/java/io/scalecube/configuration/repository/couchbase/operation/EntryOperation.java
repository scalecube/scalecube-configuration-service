package io.scalecube.configuration.repository.couchbase.operation;

import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.document.JsonDocument;
import io.scalecube.configuration.repository.Document;
import io.scalecube.configuration.repository.Repository;
import io.scalecube.configuration.repository.couchbase.ConfigurationBucketName;
import io.scalecube.configuration.repository.couchbase.JacksonTranslationService;
import io.scalecube.configuration.repository.couchbase.PasswordGenerator;
import io.scalecube.configuration.repository.couchbase.TranslationService;
import io.scalecube.configuration.repository.exception.DataAccessException;
import io.scalecube.configuration.repository.exception.DataAccessResourceFailureException;
import io.scalecube.configuration.repository.exception.KeyNotFoundException;
import io.scalecube.configuration.repository.exception.RepositoryNotFoundException;
import java.util.Objects;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import rx.RxReactiveStreams;

/**
 * An abstract base class of an entry CRUD operation in a couchbase bucket.
 *
 * @param <R> return type of the {@link #execute(OperationContext)} operation
 */
public abstract class EntryOperation<R extends Publisher> {

  protected static Logger logger;

  private final TranslationService translationService;

  public enum OperationType {
    Read,
    Write,
    Delete,
    List
  }

  EntryOperation() {
    logger = LoggerFactory.getLogger(getClass());
    translationService = new JacksonTranslationService();
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
  public abstract R execute(OperationContext context);

  Mono<Document> getDocument(AsyncBucket bucket, String id) {
    return Mono.fromRunnable(
        () -> logger.debug("enter: getDocument -> bucket = [ {} ], [ {} ]", bucket.name(), id))
        .then(Mono.from(RxReactiveStreams.toPublisher(bucket.get(id))))
        .switchIfEmpty(Mono.error(new KeyNotFoundException(id)))
        .map(jsonDocument -> toEntity(id, bucket.name(), jsonDocument))
        .onErrorMap(
            th -> !(th instanceof DataAccessException),
            th ->
                new DataAccessResourceFailureException(
                    String.format(
                        "Failed to get document with id: '%s' from bucket: '%s'",
                        id, bucket.name()),
                    th))
        .doOnError(
            th ->
                logger.error(
                    "Failed to get document with id: '{}' from bucket: '{}'",
                    id,
                    bucket.name(),
                    th))
        .doOnSuccess(
            document ->
                logger.debug(
                    "exit: getDocument -> bucket = [ {} ], [ {} ]", bucket.name(), document.key()));
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

  Mono<AsyncBucket> openBucket(OperationContext context) {
    return Mono.fromRunnable(() -> Objects.requireNonNull(context, "context"))
        .then(Mono.fromCallable(() -> getBucketName(context)))
        .doOnNext(bucketName -> logger.debug("enter: openBucket -> name = [ {} ]", bucketName))
        .flatMap(
            bucketName ->
                Mono.from(
                        RxReactiveStreams.toPublisher(
                            context
                                .cluster()
                                .openBucket(bucketName, PasswordGenerator.md5Hash(bucketName))))
                    .onErrorMap(
                        th ->
                            new RepositoryNotFoundException(
                                String.format("Failed to open bucket: '%s'", bucketName), th))
                    .doOnError(
                        th ->
                            logger.error("Failed to open bucket: '{}', error: {}", bucketName, th)))
        .doOnSuccess(bucket -> logger.debug("exit: openBucket -> name = [ {} ]", bucket.name()));
  }

  private static String getBucketName(OperationContext context) {
    if (context.repository() == null && context.key() == null) {
      throw new IllegalStateException("repository is missing");
    }

    Repository repository =
        context.repository() == null ? context.key().repository() : context.repository();

    return ConfigurationBucketName.from(repository, context.settings()).name();
  }

  Document decode(String content) {
    return translationService.decode(content, Document.class);
  }

  String encode(Document document) {
    return translationService.encode(document);
  }
}
