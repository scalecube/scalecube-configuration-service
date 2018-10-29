package io.scalecube.configuration.repository.couchbase;

import static com.couchbase.client.java.query.Select.select;
import static com.couchbase.client.java.query.dsl.Expression.i;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.RawJsonDocument;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.SimpleN1qlQuery;
import io.scalecube.configuration.repository.ConfigurationDataAccess;
import io.scalecube.configuration.repository.Document;
import io.scalecube.configuration.repository.Repository;
import io.scalecube.configuration.repository.RepositoryEntryKey;
import io.scalecube.configuration.repository.exception.DataAccessException;
import io.scalecube.configuration.repository.exception.DataAccessResourceFailureException;
import io.scalecube.configuration.repository.exception.DataRetrievalFailureException;
import io.scalecube.configuration.repository.exception.DuplicateRepositoryException;
import io.scalecube.configuration.repository.exception.KeyNotFoundException;
import io.scalecube.configuration.repository.exception.OperationInterruptedException;
import io.scalecube.configuration.repository.exception.QueryTimeoutException;
import io.scalecube.configuration.repository.exception.RepositoryNotFoundException;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

public class CouchbaseDataAccess extends CouchbaseOperations implements ConfigurationDataAccess {

  private static Logger logger = LoggerFactory.getLogger(CouchbaseDataAccess.class);

  private final Cluster cluster;
  private final CouchbaseAdmin couchbaseAdmin;
  private final TranslationService translationService = new JacksonTranslationService();

  /**
   * Create couchbase data access operations instance.
   *
   * @param settings couchbase settings
   * @param cluster couchbase cluster
   * @param couchbaseAdmin couchbase operations with admin permissions
   */
  public CouchbaseDataAccess(
      CouchbaseSettings settings, Cluster cluster, CouchbaseAdmin couchbaseAdmin) {
    super(settings);
    this.cluster = cluster;
    this.couchbaseAdmin = couchbaseAdmin;
  }

  @Override
  public boolean createRepository(Repository repository) {
    return execute(() -> createRepository0(repository));
  }

  private boolean createRepository0(Repository repository) {
    logger.debug(
        "enter: createBucket -> repository = [ {} ]", repository);
    String bucket = null;

    try {
      bucket = ConfigurationBucketName.from(repository, settings).name();
      ensureBucketNameIsNotInUse(bucket);
      couchbaseAdmin.createBucket(bucket);
    } catch (Throwable ex) {
      String message = String.format("Failed to create name: '%s'", bucket);
      handleException(ex, message);
    }

    logger.debug(
        "exit: createBucket -> repository = [ {} ]", repository);
    return true;
  }

  private void ensureBucketNameIsNotInUse(String name) {
    if (couchbaseAdmin.isBucketExists(name)) {
      throw new DuplicateRepositoryException("name with name: '" + name + " already exists.");
    }
  }

  @Override
  public Document get(RepositoryEntryKey key) {
    return execute(() -> get0(key));
  }

  private Document get0(RepositoryEntryKey key) {
    logger.debug("enter: get -> key = [{}]", key);
    String bucketName = null;
    Bucket bucket;
    Document document = null;

    try {
      bucketName = ConfigurationBucketName.from(key.repository(), settings).name();
      bucket = openBucket(bucketName);
      document = getDocument(bucket, key.key());
    } catch (Throwable ex) {
      String message =
          String.format("Failed to get key: '%s' value from name: '%s'", key, bucketName);
      handleException(ex, message);
    }

    logger.debug(
        "exit: get -> [ {} ] return -> [ {} ]",
        bucketName + "/" + key.key(),
        document != null ? document.value() : null);
    return document;
  }

  private Document getDocument(Bucket bucket, String id) {
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

  @Override
  public Document put(RepositoryEntryKey key, Document document) {
    return put0(key, document);
  }

  private Document put0(RepositoryEntryKey key, Document document) {
    logger.debug(
        "enter: put -> key = [{}], document = [{}]",
        key,
        document);

    Objects.requireNonNull(key);
    Objects.requireNonNull(document);
    String bucketName = null;

    try {
      bucketName = ConfigurationBucketName.from(key.repository(), settings).name();
      Bucket bucket = openBucket(bucketName);
      bucket.upsert(RawJsonDocument.create(key.key(), translationService.encode(document)));
    } catch (Throwable throwable) {
      String message =
          String.format("Failed to put key: '%s' value in name: '%s'", key, bucketName);
      handleException(throwable, message);
    }

    logger.debug(
        "exit: put -> key = [{}], document = [{}]",
        key,
        document);

    return document;
  }

  @Override
  public String remove(RepositoryEntryKey key) {
    return execute(() -> remove0(key));
  }

  private String remove0(RepositoryEntryKey key) {
    logger.debug(
        "enter: remove -> key = [{}]",
        key);

    Objects.requireNonNull(key);
    String bucketName = null;
    String id = null;

    try {
      bucketName = ConfigurationBucketName.from(key.repository(), settings).name();
      Bucket bucket = openBucket(bucketName);
      id = bucket.remove(key.key()).id();
    } catch (Throwable throwable) {
      String message =
          String.format("Failed to remove key: '%s' from name: '%s'", key, bucketName);
      handleException(throwable, message);
    }

    logger.debug(
        "exit: remove -> key = [{}]",
        key);
    return id;
  }

  @Override
  public Collection<Document> entries(Repository repository) {
    logger.debug("enter: entries -> repository = [ {} ]", repository);
    Collection<Document> entries;
    String bucketName = null;

    try {
      bucketName = ConfigurationBucketName.from(repository, settings).name();
      final Bucket bucket = openBucket(bucketName);
      final SimpleN1qlQuery query = N1qlQuery.simple(select("*").from(i(bucket.name())));
      entries =
          executeAsync(bucket.async().query(query))
              .flatMap(
                  result ->
                      result
                          .rows()
                          .mergeWith(
                              result
                                  .errors()
                                  .flatMap(
                                      error ->
                                          Observable.error(
                                              new DataRetrievalFailureException(
                                                  "N1QL error: " + error.toString()))))
                          .flatMap(
                              row ->
                                  Observable.just(
                                      translationService.decode(
                                          row.value().get(bucket.name()).toString(),
                                          Document.class)))
                          .toList())
              .toBlocking()
              .single();
    } catch (Throwable throwable) {
      String message = String.format("Failed to get entries from name: '%s'", bucketName);
      return handleException(throwable, message);
    }
    logger.debug(
        "exit: entries -> repository = [ {} ], return = [ {} ] entries",
        repository,
        entries.size());
    return entries;
  }

  private Collection<Document> handleException(Throwable throwable, String message) {
    logger.error(message, throwable);
    if (throwable instanceof DataAccessException) {
      throw (DataAccessException) throwable;
    } else if (throwable instanceof RuntimeException) {
      throw exceptionTranslator.translateExceptionIfPossible((RuntimeException) throwable);
    }
    throw new DataAccessResourceFailureException(message, throwable);
  }

  private <R> Observable<R> executeAsync(Observable<R> asyncAction) {
    return asyncAction.onErrorResumeNext(
        e -> {
          if (e instanceof RuntimeException) {
            return Observable.error(
                exceptionTranslator.translateExceptionIfPossible((RuntimeException) e));
          } else if (e instanceof TimeoutException) {
            return Observable.error(new QueryTimeoutException(e.getMessage(), e));
          } else if (e instanceof InterruptedException) {
            return Observable.error(new OperationInterruptedException(e.getMessage(), e));
          } else if (e instanceof ExecutionException) {
            return Observable.error(new OperationInterruptedException(e.getMessage(), e));
          } else {
            return Observable.error(e);
          }
        });
  }

  private Bucket openBucket(String name) {
    logger.debug("enter: openBucket -> name = [ {} ]", name);
    Bucket bucket;
    try {
      bucket = cluster.openBucket(name, PasswordGenerator.md5Hash(name));
    } catch (Throwable throwable) {
      logger.error("Failed to open bucket: '{}'", name);
      throw new RepositoryNotFoundException(
          String.format("Failed to open bucket: '%s'", name), throwable);
    }
    logger.debug("exit: openBucket -> name = [ {} ]", bucket.name());
    return bucket;
  }
}
