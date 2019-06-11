package io.scalecube.configuration;

import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import java.time.Duration;
import java.util.Random;
import java.util.UUID;
import org.junit.Test;
import org.junit.jupiter.api.Disabled;
import reactor.core.publisher.Mono;
import rx.RxReactiveStreams;

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
  private static final int KEYS_AMOUNT = 10;
  private static final int MAX_VERSION_NUMBER = 15;

  private static final String ORG_REPO_IDS_NAME = "ORG_ID::REPO_ID";

  public static void addDocumentsAndRecords() {
    for (int repoIndex = 0; repoIndex < REPOS_COUNT; repoIndex++) {

      String repoName = ORG_REPO_IDS_NAME + repoIndex;

      for (int keyIndex = 0; keyIndex < KEYS_AMOUNT; keyIndex++) {
        String docId = repoName + "::key" + keyIndex;

        Mono.from(
            RxReactiveStreams.toPublisher(
                bucket.insert(JsonDocument.create(docId, JsonObject.empty()))))
            .block();

        int versionAmount = random.nextInt(MAX_VERSION_NUMBER) + 1;
        for (int versionIndex = 1; versionIndex <= versionAmount; versionIndex++) {

          String currentVersion = "version " + versionIndex;

          JsonObject json = JsonObject.create();
          json.put("key " + UUID.randomUUID().toString(), "value " + UUID.randomUUID().toString());

          Mono.from(
              RxReactiveStreams
                  .toPublisher(bucket.mapAdd(docId, currentVersion, json)))
              .block();
        }
        Mono.from(
            RxReactiveStreams
                .toPublisher(bucket.mapAdd(docId, "version LAST", versionAmount)))
            .block();
      }
    }
  }


  public static void removeDocuments() {
    for (int repoIndex = 0; repoIndex < REPOS_COUNT; repoIndex++) {

      String repoName = ORG_REPO_IDS_NAME + repoIndex;

      for (int keyIndex = 0; keyIndex < KEYS_AMOUNT; keyIndex++) {
        String docId = repoName + "::key" + keyIndex;
        Mono.from(
            RxReactiveStreams.toPublisher(
                bucket.remove(docId)))
            .block();
      }
    }
  }
}

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
}
