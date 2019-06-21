package io.scalecube.configuration.repository.couchbase;

import com.couchbase.client.java.AsyncBucket;
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
import java.util.concurrent.atomic.AtomicInteger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rx.Observable;
import rx.RxReactiveStreams;

public class CouchbaseRepository implements ConfigurationRepository {

  private static final String REPOSITORY_ALREADY_EXISTS =
      "Repository with name: '%s' already exists";
  private static final String REPOSITORY_NOT_FOUND = "Repository '%s-%s' not found";

  private static final String DELIMITER = "::";

  private static final String REPOS = "repos";

  private final AsyncBucket bucket;

  public CouchbaseRepository(AsyncBucket bucket) {
    this.bucket = bucket;
  }

  @Override
  public Mono<Boolean> createRepository(Repository repository) {
    return Mono.from(RxReactiveStreams.toPublisher(bucket.setAdd(REPOS, repository.name())))
        .map(
            isNewRepoAdded -> {
              if (isNewRepoAdded) {
                return true;
              }
              throw new DocumentAlreadyExistsException();
            })
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
                    version != null ? version - 1 : -1,
                    Object.class)))
        .onErrorMap(
            DocumentDoesNotExistException.class,
            e ->
                new KeyNotFoundException(
                    String.format(
                        "Repository [%s-%s] key [%s] not found", tenant, repository, key)))
        .onErrorMap(
            PathNotFoundException.class,
            e ->
                new KeyVersionNotFoundException(
                    String.format(
                        "Key '%s' version '%s' not found",
                        key, version != null ? version : "latest")))
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
  public Flux<Document> readAll(String tenant, String repository, Integer version) {
    return Flux.from(
            RxReactiveStreams.toPublisher(
                bucket.query(
                    ViewQuery.from("keys", "by_keys").key(tenant + DELIMITER + repository))))
        .flatMap(asyncViewResult -> RxReactiveStreams.toPublisher(asyncViewResult.rows()))
        .flatMap(
            asyncViewRow -> read(tenant, repository, asyncViewRow.id().split("::")[2], version))
        .onErrorMap(CouchbaseExceptionTranslator::translateExceptionIfPossible);
  }

  @Override
  public Flux<HistoryDocument> readHistory(String tenant, String repository, String key) {
    AtomicInteger currentVersion = new AtomicInteger(0);
    return Flux.from(
            RxReactiveStreams.toPublisher(
                bucket
                    .get(docId(tenant, repository, key), JsonArrayDocument.class)
                    .switchIfEmpty(
                        Observable.defer(
                            () ->
                                Observable.error(
                                    new KeyNotFoundException(
                                        String.format(
                                            "Repository '%s-%s' key '%s' not found",
                                            tenant, repository, key)))))
                    .map(jsonDocument -> jsonDocument.content())
                    .flatMap(content -> Observable.from(content.toList()))
                    .map(entry -> new HistoryDocument(currentVersion.incrementAndGet(), entry))))
        .onErrorMap(CouchbaseExceptionTranslator::translateExceptionIfPossible);
  }

  @Override
  public Mono<Document> save(String tenant, String repository, Document document) {
    return Mono.from(RxReactiveStreams.toPublisher(bucket.setContains(REPOS, repository)))
        .flatMap(
            isRepoExists -> {
              if (!isRepoExists) {
                throw new RepositoryNotFoundException(
                    String.format(REPOSITORY_NOT_FOUND, tenant, repository));
              }
              return Mono.from(
                      RxReactiveStreams.toPublisher(
                          bucket.insert(
                              JsonArrayDocument.create(
                                  docId(tenant, repository, document.key()),
                                  JsonArray.create().add(document.value())))))
                  .onErrorMap(
                      DocumentAlreadyExistsException.class,
                      e ->
                          new RepositoryKeyAlreadyExistsException(
                              String.format(
                                  "Repository '%s-%s' key '%s' already exists",
                                  tenant, repository, document.key())))
                  .onErrorMap(CouchbaseExceptionTranslator::translateExceptionIfPossible)
                  .map(
                      documentAdded -> {
                        if (documentAdded != null) {
                          return document;
                        }

                        throw new DataAccessException(
                            "Save operation is failed because of unknown reason");
                      });
            });
  }

  @Override
  public Mono<Document> update(String tenant, String repository, Document document) {
    return Mono.from(
            RxReactiveStreams.toPublisher(
                bucket.listAppend(docId(tenant, repository, document.key()), document.value())))
        .onErrorMap(
            DocumentDoesNotExistException.class,
            e ->
                new DocumentDoesNotExistException(
                    String.format(
                        "Repository [%s-%s] key [%s] not found",
                        tenant, repository, document.key())))
        .onErrorMap(CouchbaseExceptionTranslator::translateExceptionIfPossible)
        .map(
            idKeyAdded -> {
              if (idKeyAdded) {
                return document;
              }

              throw new DataAccessException("Save operation is failed because of unknown reason");
            });
  }

  @Override
  public Mono<Void> delete(String tenant, String repository, String key) {
    return Mono.from(RxReactiveStreams.toPublisher(bucket.remove(docId(tenant, repository, key))))
        .onErrorMap(
            DocumentDoesNotExistException.class,
            e ->
                new KeyNotFoundException(
                    String.format(
                        "Repository '%s-%s' key '%s' not found", tenant, repository, key)))
        .onErrorMap(CouchbaseExceptionTranslator::translateExceptionIfPossible)
        .doOnNext(
            deletedDocument -> {
              if (deletedDocument == null) {
                throw new DataAccessException(
                    "Delete operation is failed because of unknown reason");
              }
            })
        .then();
  }

  private String docId(String tenant, String repository, String key) {
    return tenant + DELIMITER + repository + DELIMITER + key;
  }
}
