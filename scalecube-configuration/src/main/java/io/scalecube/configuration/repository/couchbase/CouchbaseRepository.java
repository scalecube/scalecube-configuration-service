package io.scalecube.configuration.repository.couchbase;

import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.document.AbstractDocument;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.error.DocumentAlreadyExistsException;
import com.couchbase.client.java.error.DocumentDoesNotExistException;
import com.couchbase.client.java.error.subdoc.PathNotFoundException;
import io.scalecube.configuration.repository.ConfigurationRepository;
import io.scalecube.configuration.repository.Document;
import io.scalecube.configuration.repository.Repository;
import io.scalecube.configuration.repository.exception.DataAccessException;
import io.scalecube.configuration.repository.exception.KeyNotFoundException;
import io.scalecube.configuration.repository.exception.RepositoryAlreadyExistsException;
import io.scalecube.configuration.repository.exception.RepositoryNotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rx.Observable;
import rx.RxReactiveStreams;

public class CouchbaseRepository implements ConfigurationRepository {

  private static final String REPOSITORY_ALREADY_EXISTS =
      "Repository with name: '%s' already exists";
  private static final String REPOSITORY_NOT_FOUND = "Repository '%s-%s' not found";
  private static final String KEY_NOT_FOUND = "Key '%s' not found";

  private static final String DELIMITER = "::";

  private final AsyncBucket bucket;

  public CouchbaseRepository(AsyncBucket bucket) {
    this.bucket = bucket;
  }

  @Override
  public Mono<Boolean> createRepository(Repository repository) {
    return Mono.from(
            RxReactiveStreams.toPublisher(
                bucket.insert(
                    JsonDocument.create(
                        repository.namespace() + DELIMITER + repository.name(),
                        JsonObject.empty()))))
        .onErrorMap(
            DocumentAlreadyExistsException.class,
            e ->
                new RepositoryAlreadyExistsException(
                    String.format(REPOSITORY_ALREADY_EXISTS, repository.name())))
        .onErrorMap(CouchbaseExceptionTranslator::translateExceptionIfPossible)
        .then(Mono.just(true));
  }

  @Override
  public Mono<Document> readEntry(String tenant, String repository, String key) {
    return Mono.from(
            RxReactiveStreams.toPublisher(
                bucket.mapGet(tenant + DELIMITER + repository, key, Object.class)))
        .onErrorMap(
            DocumentDoesNotExistException.class,
            e ->
                new RepositoryNotFoundException(
                    String.format(REPOSITORY_NOT_FOUND, tenant, repository)))
        .onErrorMap(
            PathNotFoundException.class,
            e -> new KeyNotFoundException(String.format(KEY_NOT_FOUND, key)))
        .onErrorMap(CouchbaseExceptionTranslator::translateExceptionIfPossible)
        .map(
            value -> {
              if (value instanceof JsonObject) {
                return new Document(key, ((JsonObject) value).toMap());
              } else if (value instanceof JsonArray) {
                return new Document(key, ((JsonArray) value).toList());
              } else {
                return new Document(key, value);
              }
            });
  }

  @Override
  public Flux<Document> readList(String tenant, String repository) {
    return Flux.from(
            RxReactiveStreams.toPublisher(
                bucket
                    .get(tenant + DELIMITER + repository)
                    .switchIfEmpty(
                        Observable.defer(
                            () ->
                                Observable.error(
                                    new RepositoryNotFoundException(
                                        String.format(REPOSITORY_NOT_FOUND, tenant, repository)))))
                    .map(AbstractDocument::content)
                    .flatMap(content -> Observable.from(content.toMap().entrySet()))
                    .map(entry -> new Document(entry.getKey(), entry.getValue()))))
        .onErrorMap(CouchbaseExceptionTranslator::translateExceptionIfPossible);
  }

  @Override
  public Mono<Document> createEntry(String tenant, String repository, Document document) {
    return Mono.from(
            RxReactiveStreams.toPublisher(
                bucket.mapAdd(tenant + DELIMITER + repository, document.key(), document.value())))
        .onErrorMap(
            DocumentDoesNotExistException.class,
            e ->
                new RepositoryNotFoundException(
                    String.format(REPOSITORY_NOT_FOUND, tenant, repository)))
        .onErrorMap(CouchbaseExceptionTranslator::translateExceptionIfPossible)
        .map(
            added -> {
              if (added) {
                return document;
              }

              throw new DataAccessException("Save operation is failed because of unknown reason");
            });
  }

  @Override
  public Mono<Void> deleteEntry(String tenant, String repository, String key) {
    return readEntry(tenant, repository, key)
        .then(
            Mono.from(
                RxReactiveStreams.toPublisher(
                    bucket.mapRemove(tenant + DELIMITER + repository, key))))
        .onErrorMap(
            DocumentDoesNotExistException.class,
            e ->
                new RepositoryNotFoundException(
                    String.format(REPOSITORY_NOT_FOUND, tenant, repository)))
        .onErrorMap(CouchbaseExceptionTranslator::translateExceptionIfPossible)
        .doOnNext(
            deleted -> {
              if (!deleted) {
                throw new DataAccessException(
                    "Delete operation is failed because of unknown reason");
              }
            })
        .then();
  }
}
