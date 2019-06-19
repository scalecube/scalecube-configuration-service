package io.scalecube.configuration;

import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.JsonArrayDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.error.DocumentDoesNotExistException;
import com.couchbase.client.java.error.subdoc.PathNotFoundException;
import com.couchbase.client.java.query.AsyncN1qlQueryResult;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.view.DefaultView;
import com.couchbase.client.java.view.DesignDocument;
import com.couchbase.client.java.view.ViewQuery;
import com.couchbase.client.java.view.ViewResult;
import io.scalecube.configuration.repository.Document;
import io.scalecube.configuration.repository.HistoryDocument;
import io.scalecube.configuration.repository.couchbase.CouchbaseExceptionTranslator;
import io.scalecube.configuration.repository.exception.DataAccessException;
import io.scalecube.configuration.repository.exception.KeyNotFoundException;
import io.scalecube.configuration.repository.exception.KeyVersionNotFoundException;
import io.scalecube.configuration.repository.exception.RepositoryNotFoundException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.junit.jupiter.api.Disabled;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rx.Observable;
import rx.RxReactiveStreams;

/**
 * USEFUL COUCHBASE QUERIES FOR TESTING:
 *
 * SELECT * FROM `configtest` use keys "ORG_ID::REPO_ID0::key2"
 *
 * INSERT INTO configtest VALUES("k0",{"name":"xyz"});
 *
 * SELECT * FROM `configtest` use keys "ORG_ID::REPO_ID0::key2" where `version 1` is not null;
 *
 * SELECT * FROM `configtest` use keys "ORG_ID::REPO_ID0::key2" where `version 1` is not null;
 *
 * select "version 1" FROM `configtest` WHERE `version 1` is not null;
 *
 * CREATE PRIMARY INDEX mybucket_primary_index ON `configtest` USING GSI;
 *
 * SELECT `version 1` FROM `configtest` use keys "ORG_ID::REPO_ID0::key2" where `version 1` is not
 * null;
 *
 * SELECT `version 1` FROM `configtest` where `version 1` is not null;
 *
 * SELECT `version 1` FROM `configtest` where `version 3` is not null and META(`configtest`).id like
 * "ORG_ID::REPO_ID1::%";
 *
 * SELECT * FROM `configtest` use keys "ORG_ID::REPO_ID0::key10"
 *
 * function (doc, meta) { if(meta.id != "repos") { emit(meta.id, null); } }
 */

class Scratch {

  private static final AsyncBucket bucket = couchbaseBucket();
  private static final Bucket bucketSync = couchbaseBucketSync();
  private static final Random random = new Random();

  private static AsyncBucket couchbaseBucket() {
    return Mono.fromCallable(() -> CouchbaseCluster.create("http://localhost:8091")
        .authenticate("admin", "123456")
        .openBucket("configurations")
        .async()).retryBackoff(3, Duration.ofSeconds(1)).block(Duration.ofSeconds(30));
  }

  private static Bucket couchbaseBucketSync() {
    return Mono.fromCallable(() -> CouchbaseCluster.create("http://localhost:8091")
        .authenticate("admin", "123456")
        .openBucket("configurations")
    ).retryBackoff(3, Duration.ofSeconds(1)).block(Duration.ofSeconds(30));
  }

  private static final int REPOS_COUNT = 5;
  private static final int KEYS_AMOUNT = 5;
  private static final int MAX_VERSION_NUMBER = 15;

  private static final String ORG_REPO_IDS_NAME = "ORG_ID::REPO_ID";

  public static void addDocumentsAndRecords() {
    for (int repoIndex = 0; repoIndex < REPOS_COUNT; repoIndex++) {

      for (int keyIndex = 0; keyIndex < KEYS_AMOUNT; keyIndex++) {
        String currentKeyPrefix = "key " + keyIndex;
        String docId = ORG_REPO_IDS_NAME + repoIndex + "::" + currentKeyPrefix;

        Mono.from(
            RxReactiveStreams.toPublisher(
//                bucket.insert(JsonDocument.create(docId))))
                bucket.insert(JsonArrayDocument.create(docId, JsonArray.create()))
            ))
            .block();

        int versionAmount = random.nextInt(MAX_VERSION_NUMBER) + 1;

        for (int versionIndex = 1; versionIndex <= versionAmount; versionIndex++) {
          Mono.from(
              RxReactiveStreams.toPublisher(
                  bucket.listAppend(docId, currentKeyPrefix + " >>> version " + versionIndex)
              )).block();
        }
      }
    }
  }


  public static void removeDocuments() {
    for (int repoIndex = 0; repoIndex < REPOS_COUNT; repoIndex++) {

      for (int keyIndex = 0; keyIndex < KEYS_AMOUNT; keyIndex++) {

        String currentKeyPrefix = "key " + keyIndex;

        String docId = ORG_REPO_IDS_NAME + repoIndex + "::" + currentKeyPrefix;

        Mono.from(
            RxReactiveStreams.toPublisher(
                bucket.remove(docId)))
            .block();
//      }
      }
    }
  }

  public static void query1() {
//    String docId = "ORG_ID::REPO_ID0::key 1";
    String docId = "repos";

//    String value =
    Boolean value =
        Mono.from(
            RxReactiveStreams.toPublisher(
//                bucket.listGet(docId, -1, String.class)
                bucket.setContains("repos", "quest")
            ))
            .block();

    System.out.println(value);
  }

  public static void query() {
    String query = "select count(*) from `configtest`";

    System.out.println(
        bucket.query(N1qlQuery.simple(query))
            .flatMap(AsyncN1qlQueryResult::rows)
            .map(result -> result.value().toMap())
            .toList()
            .timeout(5, TimeUnit.SECONDS)
            .toBlocking()
            .single()
    );
  }

  public static void temp() {

    List<String> list = new ArrayList<>();
    list.add("value " + UUID.randomUUID().toString());
    list.add("value " + UUID.randomUUID().toString());
    list.add("value " + UUID.randomUUID().toString());

    JsonObject json = JsonObject.create();
    json.put("key " + UUID.randomUUID().toString(), "value " + UUID.randomUUID().toString());
    json.put("key " + UUID.randomUUID().toString(), list);

    String docId = "ORG_ID::REPO_ID0::key-1";
    Mono.from(
        RxReactiveStreams
            .toPublisher(
                bucket.mapGet(docId, "keys", JsonArray.class)))
        .flatMap(a -> {
          a.add("newk");
          return Mono.from(
              RxReactiveStreams
                  .toPublisher(

                      bucket.mapAdd(docId, "keys", a)));
        })
        .block();
    Flux<String> f = Flux.just("1", "2", "3");

    Flux.just("a", "b", "c").delayElements(Duration.ofMillis(500))
        .zipWith(f, (a, b) -> a + ">>>" + b).subscribe(System.out::println);
  }

  public static void tempRemoveKey() {
    Mono.from(
        RxReactiveStreams
            .toPublisher(
                bucket.mapGet("ORG_ID::REPO_ID1", "keys", JsonArray.class)))
        .flatMap(a -> {
          JsonArray ja = JsonArray.create();

          a.forEach(e -> {
            if (!e.equals("key 2")) {
              ja.add(e);
            }
          });

          return Mono.from(
              RxReactiveStreams
                  .toPublisher(bucket.mapAdd("ORG_ID::REPO_ID1", "keys", ja)));
        }).block();
  }

  public static void sequentialQuery() {
    Mono.from(
        RxReactiveStreams.toPublisher(
            bucket.mapAdd("ORG_ID::REPO_ID1", "abc", "one")))
        .flatMap(a ->
            Mono.from(
                RxReactiveStreams
                    .toPublisher(bucket.mapAdd("ORG_ID::REPO_ID1", "abc2", "two")))
        )
        .onErrorMap(
            DocumentDoesNotExistException.class,
            e ->
                new RepositoryNotFoundException("no found"))
        .onErrorMap(CouchbaseExceptionTranslator::translateExceptionIfPossible)
        .map(
            added -> {
              if (added) {
                return "abc";
              }

              throw new DataAccessException("Save operation is failed because of unknown reason");
            })
        .block();
  }

  public static void createView() {

    String pref = "a3_sdf";
    Mono.from(
        RxReactiveStreams.toPublisher(
            bucket.setAdd("repos", pref)
        ))
        .flatMap(isNewRepoAdded -> {
          if (isNewRepoAdded) {
            return Mono.from(RxReactiveStreams.toPublisher(
                bucket.bucketManager().flatMap(bucketManager ->
                    bucketManager.insertDesignDocument(DesignDocument.create(
                        "dev_repos_keys_" + pref,
                        Arrays.asList(
                            DefaultView.create("repo_keys: " + "configurations " + pref,
                                "function (doc, meta) {"
                                    + "if (meta.id != 'repos' && meta.id.includes('"
                                    + "configurations " + pref + "'))"
                                    + "{ emit(meta.id); } "
                                    + "}")
                        )
                    )).flatMap(designDocument -> bucketManager
                        .publishDesignDocument("repos_keys_" + pref, true)
                    ).map(insertResult -> true)
                )));
          }
          return Mono.just(false);
        }).subscribe(e -> System.err.println("ttt: " + e));
  }

  public static void parametrizedView() {
    ViewQuery viewQuery = ViewQuery.from("keys", "by_keys").key("ORG-A82A94382D9E5CBA82BC::REPO1");
    ViewResult query = bucketSync.query(viewQuery);
    System.out.println(query.totalRows());
    query.allRows().forEach(e -> System.out.println(e));
  }

  public static void queryVersion() {
    Integer version = 1;

    Mono.from(
        RxReactiveStreams.toPublisher(
            bucket.listGet("ORG-D6C68254A98A28294F44::REPO 2::KEY3",
                version != null ? version - 1 : -1,
                Object.class))
    )
        .onErrorMap(
            DocumentDoesNotExistException.class,
            e ->
                new KeyNotFoundException(
                    String
                        .format("Repository [%s-%s] key [%s] not found", 1, 2, 3)))
        .onErrorMap(
            PathNotFoundException.class,
            e -> new KeyVersionNotFoundException(
                String.format("Key '%s' version '%s' not found", 3, version)))
        .onErrorMap(CouchbaseExceptionTranslator::translateExceptionIfPossible)
        .map(
            value -> {
              if (value instanceof JsonObject) {
                return new Document("key", ((JsonObject) value).toMap());
              } else if (value instanceof JsonArray) {
                return new Document("key", ((JsonArray) value).toList());
              } else {
                return new Document("key", value);
              }
            })
        .subscribe(System.out::println);
  }

  public static AtomicInteger versionNumber = new AtomicInteger(0);

  public static void queryEntryHistory() {

    Flux.from(
        RxReactiveStreams.toPublisher(
            bucket
                .get("ORG-684552FB4637C45DD8BA::REPO1::KEY1", JsonArrayDocument.class)
                .switchIfEmpty(
                    Observable.defer(
                        () ->
                            Observable.error(
                                new KeyNotFoundException(
                                    String.format("key not found")))))
                .map(jsonDocument -> jsonDocument.content())
                .flatMap(content -> Observable.from(content.toList()))
                .map(entry -> new HistoryDocument(versionNumber.incrementAndGet(), entry))))
        .onErrorMap(CouchbaseExceptionTranslator::translateExceptionIfPossible)
        .subscribe(System.out::println);
  }

  public static void readList() {

    Flux.from(
        RxReactiveStreams.toPublisher(
            bucket
                .query(ViewQuery.from("keys", "by_keys").key("ORG-73C86327B252848760AD::REPO1"))
        ))
        .flatMap(asyncViewResult ->
            RxReactiveStreams.toPublisher(asyncViewResult.rows())
        )
        .flatMap(asyncViewRow -> readEntry(asyncViewRow.id(), 1))
        .subscribe(System.out::println);
  }

  private static Mono<Document> readEntry(String repoAndKey, Integer version) {
    return
        Mono.from(
            RxReactiveStreams.toPublisher(
                bucket.listGet(repoAndKey, version != null ? version - 1 : -1,
                    Object.class))
        )
            .onErrorMap(
                DocumentDoesNotExistException.class,
                e ->
                    new KeyNotFoundException(
                        String
                            .format("Repository key not found", repoAndKey)))
            .onErrorMap(
                PathNotFoundException.class,
                e -> new KeyVersionNotFoundException(
                    String.format("Repo key '%s' version '%s' not found", repoAndKey,
                        version != null ? version : "latest")))
            .onErrorMap(CouchbaseExceptionTranslator::translateExceptionIfPossible)
            .map(
                value -> {
                  if (value instanceof JsonObject) {
                    return new Document(repoAndKey, ((JsonObject) value).toMap());
                  } else if (value instanceof JsonArray) {
                    return new Document(repoAndKey, ((JsonArray) value).toList());
                  } else {
                    return new Document(repoAndKey, value);
                  }
                });
  }
}

/**
 * todo: should be deleted after experiments
 */
@Disabled
public class CouchbaseExperimentalTest {

  @Test
  public void createTest() {
    Scratch.addDocumentsAndRecords();
  }

  @Test
  public void deleteTest() {
    Scratch.removeDocuments();
  }

  @Test
  public void query() {
    Scratch.query();
  }

  @Test
  public void temp() {
    Scratch.temp();
  }

  @Test
  public void query1() {
    Scratch.query1();
  }

  @Test
  public void tempRemoveKey() {
    Scratch.tempRemoveKey();
  }

  @Test
  public void sequentialQuery() {
    Scratch.sequentialQuery();
  }

  @Test
  public void createView() throws Exception {
    Scratch.createView();

    Thread.sleep(2000);
  }

  @Test
  public void parametrizedView() throws Exception {
    Scratch.parametrizedView();

    Thread.sleep(2000);
  }

  @Test
  public void queryVersion() throws Exception {
    Scratch.queryVersion();

    Thread.sleep(2000);
  }

  @Test
  public void queryEntryHistory() throws Exception {
    Scratch.queryEntryHistory();

    Thread.sleep(2000);
  }

  @Test
  public void readList() throws Exception {
    Scratch.readList();

    Thread.sleep(2000);
  }
}
