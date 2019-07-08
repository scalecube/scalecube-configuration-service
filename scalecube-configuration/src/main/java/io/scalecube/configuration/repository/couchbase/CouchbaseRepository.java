package io.scalecube.configuration.repository.couchbase;

import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.document.AbstractDocument;
import com.couchbase.client.java.document.JsonArrayDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.error.DocumentAlreadyExistsException;
import com.couchbase.client.java.error.DocumentDoesNotExistException;
import com.couchbase.client.java.error.subdoc.PathNotFoundException;
import com.couchbase.client.java.view.ViewQuery;
import io.scalecube.configuration.repository.ConfigurationRepository;
import io.scalecube.configuration.repository.Document;
import io.scalecube.configuration.repository.HistoryDocument;
import io.scalecube.configuration.repository.Repository;
import io.scalecube.configuration.repository.exception.DataAccessException;
import io.scalecube.configuration.repository.exception.KeyNotFoundException;
import io.scalecube.configuration.repository.exception.KeyVersionNotFoundException;
import io.scalecube.configuration.repository.exception.RepositoryAlreadyExistsException;
import io.scalecube.configuration.repository.exception.RepositoryKeyAlreadyExistsException;
import io.scalecube.configuration.repository.exception.RepositoryNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rx.RxReactiveStreams;

public class CouchbaseRepository implements ConfigurationRepository {

  private static final String REPOSITORY_ALREADY_EXISTS =
      "Repository with name: '%s' already exists";
  private static final String REPOSITORY_NOT_FOUND = "Repository '%s-%s' not found";

  private static final String DELIMITER = "::";

  private static final String REPOS = "repos";
  private static final int DEFAULT_LATEST_VERSION = -1;
  private static final int INDEX_OF_KEY = 2;

  private final AsyncBucket bucket;

  public CouchbaseRepository(AsyncBucket bucket) {
    this.bucket = bucket;
  }

  @Override
  public Mono<Boolean> createRepository(Repository repository) {
    return Mono.from(
            RxReactiveStreams.toPublisher(
                bucket.setAdd(REPOS, repository.namespace() + DELIMITER + repository.name())))
        .filter(isNewRepoAdded -> isNewRepoAdded)
        .switchIfEmpty(
            Mono.error(
                () ->
                    new RepositoryAlreadyExistsException(
                        String.format(REPOSITORY_ALREADY_EXISTS, repository.name()))))
        .onErrorMap(
            DocumentAlreadyExistsException.class,
            e ->
                new RepositoryAlreadyExistsException(
                    String.format(REPOSITORY_ALREADY_EXISTS, repository.name())))
        .onErrorMap(CouchbaseExceptionTranslator::translateExceptionIfPossible);
  }

  @Override
  public Mono<Document> read(String tenant, String repository, String key, Integer version) {
    return Mono.from(
            RxReactiveStreams.toPublisher(
                bucket.listGet(
                    docId(tenant, repository, key),
                    version != null ? version - 1 : DEFAULT_LATEST_VERSION,
                    Object.class)))
        .onErrorMap(
            DocumentDoesNotExistException.class,
            e ->
                new KeyNotFoundException(
                    String.format("Repository '%s' key '%s' not found", repository, key)))
        .onErrorMap(
            PathNotFoundException.class,
            e ->
                new KeyVersionNotFoundException(
                    String.format(
                        "Key '%s' version '%s' not found",
                        key, version != null ? version : "latest")))
        .onErrorMap(CouchbaseExceptionTranslator::translateExceptionIfPossible)
        .map(value -> new Document(key, readJsonValue(value)));
  }

  @Override
  public Flux<Document> readAll(String tenant, String repository, Integer version) {
    return Mono.from(
            RxReactiveStreams.toPublisher(
                bucket.setContains(REPOS, tenant + DELIMITER + repository)))
        .filter(isRepoExists -> isRepoExists)
        .switchIfEmpty(
            Mono.error(
                () ->
                    new RepositoryNotFoundException(
                        String.format(REPOSITORY_NOT_FOUND, tenant, repository))))
        .thenMany(
            Flux.from(
                RxReactiveStreams.toPublisher(
                    bucket.query(
                        ViewQuery.from("keys", "by_keys").key(tenant + DELIMITER + repository)))))
        .flatMap(asyncViewResult -> RxReactiveStreams.toPublisher(asyncViewResult.rows()))
        .flatMap(
            asyncViewRow ->
                read(tenant, repository, asyncViewRow.id().split(DELIMITER)[INDEX_OF_KEY], version))
        .onErrorContinue(
            KeyVersionNotFoundException.class,
            (e, o) -> {
              // no-op
            })
        .onErrorMap(CouchbaseExceptionTranslator::translateExceptionIfPossible);
  }

  @Override
  public Flux<HistoryDocument> readHistory(String tenant, String repository, String key) {
    AtomicInteger currentVersion = new AtomicInteger(0);
    return Mono.from(
            RxReactiveStreams.toPublisher(
                bucket.get(docId(tenant, repository, key), JsonArrayDocument.class)))
        .switchIfEmpty(
            Mono.error(
                () ->
                    new KeyNotFoundException(
                        String.format("Repository '%s' key '%s' not found", repository, key))))
        .map(AbstractDocument::content)
        .flatMapIterable(
            objects ->
                objects.toList().stream()
                    .map(
                        value ->
                            new HistoryDocument(
                                currentVersion.incrementAndGet(), readJsonValue(value)))
                    .collect(Collectors.toList()))
        .onErrorMap(CouchbaseExceptionTranslator::translateExceptionIfPossible);
  }

  @Override
  public Mono<Document> save(String tenant, String repository, Document document) {
    return Mono.from(
            RxReactiveStreams.toPublisher(
                bucket.setContains(REPOS, tenant + DELIMITER + repository)))
        .filter(isRepoExists -> isRepoExists)
        .switchIfEmpty(
            Mono.error(
                () ->
                    new RepositoryNotFoundException(
                        String.format(REPOSITORY_NOT_FOUND, tenant, repository))))
        .then(
            Mono.from(
                RxReactiveStreams.toPublisher(
                    bucket.insert(
                        JsonArrayDocument.create(
                            docId(tenant, repository, document.key()),
                            JsonArray.create().add(savedJsonValue(document.value())))))))
        .onErrorMap(
            DocumentAlreadyExistsException.class,
            e ->
                new RepositoryKeyAlreadyExistsException(
                    String.format(
                        "Repository '%s' key '%s' already exists", repository, document.key())))
        .onErrorMap(CouchbaseExceptionTranslator::translateExceptionIfPossible)
        .switchIfEmpty(
            Mono.error(
                () ->
                    new DataAccessException("Save operation is failed because of unknown reason")))
        .thenReturn(document);
  }

  @Override
  public Mono<Document> update(String tenant, String repository, Document document) {
    return Mono.from(
            RxReactiveStreams.toPublisher(
                bucket.listAppend(docId(tenant, repository, document.key()), document.value())))
        .filter(isUpdated -> isUpdated)
        .switchIfEmpty(
            Mono.error(
                () ->
                    new DataAccessException("Save operation is failed because of unknown reason")))
        .then(
            Mono.from(
                RxReactiveStreams.toPublisher(
                    bucket.listSize(docId(tenant, repository, document.key())))))
        .onErrorMap(
            DocumentDoesNotExistException.class,
            e ->
                new DocumentDoesNotExistException(
                    String.format(
                        "Repository '%s' key '%s' not found", repository, document.key())))
        .onErrorMap(CouchbaseExceptionTranslator::translateExceptionIfPossible)
        .map(
            lastVersion ->
                new Document(document.key(), savedJsonValue(document.value()), lastVersion));
  }

  @Override
  public Mono<Void> delete(String tenant, String repository, String key) {
    return Mono.from(RxReactiveStreams.toPublisher(bucket.remove(docId(tenant, repository, key))))
        .onErrorMap(
            DocumentDoesNotExistException.class,
            e ->
                new KeyNotFoundException(
                    String.format("Repository '%s' key '%s' not found", repository, key)))
        .onErrorMap(CouchbaseExceptionTranslator::translateExceptionIfPossible)
        .switchIfEmpty(
            Mono.error(
                () ->
                    new DataAccessException(
                        "Delete operation is failed because of unknown reason")))
        .then();
  }

  private String docId(String tenant, String repository, String key) {
    return tenant + DELIMITER + repository + DELIMITER + key;
  }

  private Object savedJsonValue(Object v) {
    if (v instanceof LinkedHashMap) {
      return JsonObject.from((Map<String, ?>) v);
    } else if (v instanceof ArrayList) {
      return JsonArray.from((ArrayList) v);
    } else if (v == null) {
      return JsonObject.NULL;
    }
    return v;
  }

  private Object readJsonValue(Object v) {
    if (v instanceof JsonObject) {
      return ((JsonObject) v).toMap();
    } else if (v instanceof JsonArray) {
      return ((JsonArray) v).toList();
    } else if (v == null || v == JsonObject.NULL) {
      return JsonObject.NULL;
    }
    return v;
  }
}
