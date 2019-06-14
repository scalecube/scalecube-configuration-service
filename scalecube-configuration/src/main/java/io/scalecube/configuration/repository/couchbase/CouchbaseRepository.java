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
import io.scalecube.configuration.repository.HistoryDocument;
import io.scalecube.configuration.repository.Repository;
import io.scalecube.configuration.repository.exception.DataAccessException;
import io.scalecube.configuration.repository.exception.KeyNotFoundException;
import io.scalecube.configuration.repository.exception.RepositoryAlreadyExistsException;
import io.scalecube.configuration.repository.exception.RepositoryNotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rx.Observable;
import rx.RxReactiveStreams;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class CouchbaseRepository implements ConfigurationRepository {

  private static final String REPOSITORY_ALREADY_EXISTS =
      "Repository with name: '%s' already exists";
  private static final String REPOSITORY_NOT_FOUND = "Repository '%s-%s' not found";
  private static final String KEY_VERSION_NOT_FOUND = "Key '%s' with version: '%s' not found";
  private static final String KEY_NOT_FOUND = "Key '%s' not found";

  private static final String DELIMITER = "::";

  private static final String VERSION_DELIMITER = ">>>";
  private static final String FIRST_VERSION_WITH_PREFIX = " " + VERSION_DELIMITER + " version 1";
  private static final String VERSION_WITH_PREFIX = " " + VERSION_DELIMITER + " version ";
  private static final String LATEST_VERSION_WITH_PREFIX =
      " " + VERSION_DELIMITER + " version LATEST";

  private static final String KEYS = "keys";

  public CouchbaseRepository(AsyncBucket bucket) {
    this.bucket = bucket;
  }

  private final AsyncBucket bucket;

  @Override
  public Mono<Boolean> createRepository(Repository repository) {
    return Mono.from(
        RxReactiveStreams.toPublisher(
            bucket.insert(
                JsonDocument.create(
                    repository.namespace() + DELIMITER + repository.name(),
                    JsonObject.create().put(KEYS, JsonArray.empty())))))
        .onErrorMap(
            DocumentAlreadyExistsException.class,
            e ->
                new RepositoryAlreadyExistsException(
                    String.format(REPOSITORY_ALREADY_EXISTS, repository.name())))
        .onErrorMap(CouchbaseExceptionTranslator::translateExceptionIfPossible)
        .then(Mono.just(true));
  }

  @Override
  public Mono<Document> readEntry(String tenant, String repository, String key, Integer version) {
    String docId = tenant + DELIMITER + repository;
    return suitableVersion(bucket, docId, key, version)
        .flatMap(suitableVersion -> Mono.from(RxReactiveStreams.toPublisher(
            bucket.mapGet(docId, key + VERSION_WITH_PREFIX + suitableVersion, Object.class))))
        .onErrorMap(
            DocumentDoesNotExistException.class,
            e ->
                new RepositoryNotFoundException(
                    String.format(REPOSITORY_NOT_FOUND, tenant, repository)))
        .onErrorMap(
            PathNotFoundException.class,
            e -> new KeyNotFoundException(
                version != null ? String.format(KEY_VERSION_NOT_FOUND, key, version)
                    : String.format(KEY_NOT_FOUND, key)))
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
  public Flux<Document> readList(String tenant, String repository, Integer version) {
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
  public Flux<HistoryDocument> readEntryHistory(String tenant, String repository, String key) {
    throw new NotImplementedException();
  }

  @Override
  public Mono<Document> createEntry(String tenant, String repository, Document document) {
    String docId = tenant + DELIMITER + repository;
    return Mono.from(
        RxReactiveStreams.toPublisher(
            bucket.mapAdd(docId, document.key() + FIRST_VERSION_WITH_PREFIX, document.value())))
        .and(
            Mono.from(
                RxReactiveStreams.toPublisher(
                    bucket
                        .mapAdd(docId, document.key() + LATEST_VERSION_WITH_PREFIX, 1))))
        .then(
            Mono.from(
                RxReactiveStreams
                    .toPublisher(
                        bucket.mapGet(docId, KEYS, JsonArray.class)))
                .flatMap(keysArr ->
                    Mono.from(RxReactiveStreams.toPublisher(
                        bucket.mapAdd(docId, KEYS, uniqueKeyAdding(keysArr, document.key()))))))
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
  public Mono<Document> updateEntry(String tenant, String repository, Document document) {
    String docId = tenant + DELIMITER + repository;

    return readEntry(tenant, repository, document.key() + LATEST_VERSION_WITH_PREFIX, null)
        .flatMap(lastVersion -> {
          int nextVersion = Integer.valueOf(document.value().toString()) + 1;
          return
              Mono.from(RxReactiveStreams.toPublisher(
                  bucket.mapAdd(docId, document.key() + LATEST_VERSION_WITH_PREFIX, nextVersion)))
                  .then(
                      Mono.from(RxReactiveStreams.toPublisher(
                          bucket.mapAdd(docId, document.key() + VERSION_WITH_PREFIX, nextVersion))
                      ));
        })
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
    return readEntry(tenant, repository, key, null)
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

  private static Mono<Integer> getLatestVersion(AsyncBucket bucket, String docId, String key) {
    return Mono.from(RxReactiveStreams
        .toPublisher(bucket.mapGet(docId, key + LATEST_VERSION_WITH_PREFIX, Integer.class)));
  }

  private static Mono<Integer> suitableVersion(AsyncBucket bucket, String docId, String key,
      Integer version) {
    if (version == null) {
      return getLatestVersion(bucket, docId, key);
    } else {
      return Mono.just(version);
    }
  }

  private static JsonArray uniqueKeyAdding(JsonArray arr, String key) {
    for (Object elem : arr) {
      if (elem.equals(key)) {
        return arr;
      }
    }
    return arr.add(key);
  }
}
