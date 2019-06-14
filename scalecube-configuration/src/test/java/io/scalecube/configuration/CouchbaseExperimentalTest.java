package io.scalecube.configuration;

import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.AsyncN1qlQueryResult;
import com.couchbase.client.java.query.Index;
import com.couchbase.client.java.query.N1qlParams;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.jupiter.api.Disabled;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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
 */

class Scratch {

  private static final AsyncBucket bucket = couchbaseBucket();
  private static final Random random = new Random();

  private static AsyncBucket couchbaseBucket() {
    return Mono.fromCallable(() -> CouchbaseCluster.create("http://localhost:8091")
        .authenticate("admin", "123456")
        .openBucket("configtest")
        .async()).retryBackoff(3, Duration.ofSeconds(1)).block(Duration.ofSeconds(30));
  }

  private static final int REPOS_COUNT = 5;
  private static final int KEYS_AMOUNT = 5;
  private static final int MAX_VERSION_NUMBER = 15;

  private static final String ORG_REPO_IDS_NAME = "ORG_ID::REPO_ID";

  public static void addDocumentsAndRecords() {
    for (int repoIndex = 0; repoIndex < REPOS_COUNT; repoIndex++) {

      String docId = ORG_REPO_IDS_NAME + repoIndex;

      Mono.from(
          RxReactiveStreams.toPublisher(
              bucket.insert(JsonDocument.create(docId, JsonObject.empty()))))
          .block();

      for (int keyIndex = 0; keyIndex < KEYS_AMOUNT; keyIndex++) {

        String currentKeyPrefix = "key " + keyIndex;

        int versionAmount = random.nextInt(MAX_VERSION_NUMBER) + 1;

        for (int versionIndex = 1; versionIndex <= versionAmount; versionIndex++) {

          String currentVersion = "version " + versionIndex;

          String currentKey = currentKeyPrefix + " >>> " + currentVersion;

          JsonObject json = JsonObject.create();
//          json.put("key " + UUID.randomUUID().toString(), "value " + UUID.randomUUID().toString());
          json.put(currentKeyPrefix, "value " + UUID.randomUUID().toString());

          Mono.from(
              RxReactiveStreams
                  .toPublisher(bucket.mapAdd(docId, currentKey, json)))
              .block();
        }

        if (keyIndex == 0) {
          Mono.from(
              RxReactiveStreams
                  .toPublisher(bucket.mapAdd(docId, "keys", JsonArray.empty())))
              .block();
        }

        Mono.from(
            RxReactiveStreams
                .toPublisher(bucket
                    .mapAdd(docId, currentKeyPrefix + " >>> " + "version LATEST", versionAmount)))
            .block();

        Mono.from(
            RxReactiveStreams
                .toPublisher(
                    bucket.mapGet(docId, "keys", JsonArray.class)))
            .flatMap(a -> {
              a.add(currentKeyPrefix);
              return Mono.from(
                  RxReactiveStreams
                      .toPublisher(bucket.mapAdd(docId, "keys", a)));
            }).block();
      }
    }
  }


  public static void removeDocuments() {
    for (int repoIndex = 0; repoIndex < REPOS_COUNT; repoIndex++) {

      String repoName = ORG_REPO_IDS_NAME + repoIndex;
//
//      for (int keyIndex = 0; keyIndex < KEYS_AMOUNT; keyIndex++) {
//        String docId = repoName + "::key" + keyIndex;
      Mono.from(
          RxReactiveStreams.toPublisher(
//                bucket.remove(docId)))
              bucket.remove(repoName)))
          .block();
//      }
    }
  }

  public static void query() {

//    N1qlParams params = N1qlParams.build().adhoc(false);
//    N1qlQuery query = N1qlQuery.simple("select count(*) from `mybucket`", params);

//    bucket.bucketManager().createN1qlPrimaryIndex(true, false)

    String query = "select count(*) from `configtest`";

//    N1qlQueryResult result = bucket.query(N1qlQuery.simple(query)).;
//    List<Map<String, Object>> data = extractResultOrThrow(result);

//    Mono.from(
//        RxReactiveStreams.toPublisher(bucket.query(N1qlQuery.simple("SELECT * FROM test")))

//    Index.createPrimaryIndex().on(bucket.name());
//
//    System.out.println(
//    (Mono.from(RxReactiveStreams.toPublisher(
//        Mono.from(
//            RxReactiveStreams.toPublisher(
//                bucket.query(N1qlQuery.simple("SELECT count(*) FROM `configtest`"))
//            )).block().rows())).block().value()));

//    Index.createPrimaryIndex().on(bucket.name());
//
//    System.out.println(
//    (Mono.from(RxReactiveStreams.toPublisher(
//        Mono.from(
//            RxReactiveStreams.toPublisher(
//                bucket.query(N1qlQuery.simple("SELECT count(*) FROM `configtest`"))
//            )).block().rows())).block().value()));

    System.out.println(
        bucket.query(N1qlQuery.simple(query))
            .flatMap(AsyncN1qlQueryResult::rows)
            .map(result -> result.value().toMap())
            .toList()
            .timeout(5, TimeUnit.SECONDS)
            .toBlocking()
            .single()
    );

//    try {
//      Thread.currentThread().join();
//    } catch (InterruptedException e) {
//      e.printStackTrace();
//    }

//    AsyncN1qlQueryResult result = bucket.query(N1qlQuery.simple("SELECT * FROM test"));
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

//    Mono.from(
//        RxReactiveStreams
////            .toPublisher(bucket.mapAdd("ORG_ID::REPO_ID0::key0", "abc", json)))
//            .toPublisher(
////                bucket.insert(JsonDocument.create("ORG_ID::REPO_ID0::key-1", json))))
////                bucket.append(JsonDocument.create("ORG_ID::REPO_ID0::key-1", json))))
//
//                bucket.mapAdd(docId, "keys", list)))
//        .block();

//    JsonArray array =
    Mono.from(
        RxReactiveStreams
//            .toPublisher(bucket.mapAdd("ORG_ID::REPO_ID0::key0", "abc", json)))
            .toPublisher(
//                bucket.insert(JsonDocument.create("ORG_ID::REPO_ID0::key-1", json))))
//                bucket.append(JsonDocument.create("ORG_ID::REPO_ID0::key-1", json))))

                bucket.mapGet(docId, "keys", JsonArray.class)))
        .flatMap(a -> {
          a.add("newk");
          return Mono.from(
              RxReactiveStreams
                  .toPublisher(

                      bucket.mapAdd(docId, "keys", a)));
        })
//        .subscribe();
//        .zipWhen(a -> {
//          System.out.println(">>> :::" + a);
//          return Mono.empty();
//        })

        .block();
    Flux<String> f = Flux.just("1", "2", "3");

    Flux.just("a", "b", "c").delayElements(Duration.ofMillis(500))
        .zipWith(f, (a, b) -> a + ">>>" + b).subscribe(System.out::println);

//    try {
//      Thread.currentThread().join();
//    } catch (InterruptedException e) {
//      e.printStackTrace();
//    }

//        .then(
//
////              a.add("key " + UUID.randomUUID().toString());
//
//              Mono.from(
//                  RxReactiveStreams
////            .toPublisher(bucket.mapAdd("ORG_ID::REPO_ID0::key0", "abc", json)))
//                      .toPublisher(
////                bucket.insert(JsonDocument.create("ORG_ID::REPO_ID0::key-1", json))))
////                bucket.append(JsonDocument.create("ORG_ID::REPO_ID0::key-1", json))))
//
//                          bucket.mapGet(docId, "keys", JsonArray.class)))
////        .block();
//            ).block();
//    System.out.println(array);
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
  public void tempRemoveKey() {
    Scratch.tempRemoveKey();
  }
}
