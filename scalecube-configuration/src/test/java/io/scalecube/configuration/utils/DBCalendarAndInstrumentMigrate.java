package io.scalecube.configuration.utils;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.JsonArrayDocument;
import com.couchbase.client.java.document.JsonLongDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.view.ViewQuery;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DBCalendarAndInstrumentMigrate {

  private static final String DEFAULT_HOST = "localhost";
  private static final String DEFAULT_PORT = "8091";
  private static final String DEFAULT_USER = "admin";
  private static final String DEFAULT_PASSWORD = "123456";
  private static final String DEFAULT_BUCKET_NAME = "configurations";

  private static final String DELIMITER = "::";

  private final String host;
  private final String port;
  private final String user;
  private final String password;
  private final String bucketName;

  private Bucket bucket;

  public DBCalendarAndInstrumentMigrate() {
    this.host = System.getProperty("host", DEFAULT_HOST);
    this.port = System.getProperty("port", DEFAULT_PORT);
    this.user = System.getProperty("user", DEFAULT_USER);
    this.password = System.getProperty("password", DEFAULT_PASSWORD);
    this.bucketName = System.getProperty("bucket", DEFAULT_BUCKET_NAME);
  }

  private Bucket getBucket() {
    return CouchbaseCluster.create(host).authenticate(user, password).openBucket(bucketName);
  }

  private Set<String> orgSet() {

    Set<String> repos =
        bucket.get("repos", JsonArrayDocument.class).content().toList().stream()
            .map(e -> ((String) e).split(DELIMITER)[0])
            .collect(Collectors.toSet());

    bucket.upsert(JsonArrayDocument.create("repos", JsonArray.empty()));

    return repos;
  }

  /**
   * @param orgId orgId
   * @return new calendarId map
   */
  private Map<String, Integer> orgIdCalendarIdsMap(String orgId) {
    Map<String, Integer> orgIdEntityIdsMap = new HashMap<>();

    String orgIdCalendar = orgId + DELIMITER + "CalendarsList";

    bucket.listAppend("repos", orgIdCalendar);

    bucket
        .query(ViewQuery.from("keys", "by_keys").key(orgIdCalendar))
        .allRows()
        .forEach(
            e -> {
              String id = e.id();
              JsonLongDocument entityCounter =
                  bucket.counter(orgId + DELIMITER + "CalendarIdCounter", 1, 1);
              orgIdEntityIdsMap.put(id.split(DELIMITER)[2], (int) (long) entityCounter.content());

              JsonArrayDocument jad = bucket.get(id, JsonArrayDocument.class);

              bucket.insert(
                  JsonArrayDocument.create(
                      orgIdCalendar + DELIMITER + entityCounter.content(), jad.content()));
              bucket.remove(id);
            });

    return orgIdEntityIdsMap;
  }

  /**
   * @param orgId orgId
   * @param calendarIdsMap map of new calendarsId
   * @return new calendarId map
   */
  private Map<String, Integer> orgIdInstrumentIdsMap(
      String orgId, Map<String, Integer> calendarIdsMap) {
    Map<String, Integer> orgIdInstrumentIdsMap = new HashMap<>();

    String orgIdInstrument = orgId + DELIMITER + "InstrumentsList";

    bucket.listAppend("repos", orgIdInstrument);

    bucket
        .query(ViewQuery.from("keys", "by_keys").key(orgIdInstrument))
        .allRows()
        .forEach(
            e -> {
              String id = e.id();
              JsonLongDocument entityCounter =
                  bucket.counter(orgId + DELIMITER + "InstrumentIdCounter", 1, 1);
              orgIdInstrumentIdsMap.put(
                  id.split(DELIMITER)[2], (int) (long) entityCounter.content());

              JsonArrayDocument jad = bucket.get(id, JsonArrayDocument.class);

              // Change calendarId inside
              jad.content()
                  .forEach(
                      el -> {
                        JsonObject arrElem = (JsonObject) el;
                        String oldCalendarId = (String) arrElem.get("calendarId");
                        arrElem.put("calendarId", calendarIdsMap.get(oldCalendarId));
                      });

              bucket.insert(
                  JsonArrayDocument.create(
                      orgIdInstrument + DELIMITER + entityCounter.content(), jad.content()));
              bucket.remove(id);
            });

    return orgIdInstrumentIdsMap;
  }

  private void start() {
    bucket = getBucket();
    Set<String> orgIdSet = orgSet();

    for (String orgId : orgIdSet) {
      Map<String, Integer> calendarIdsMap = orgIdCalendarIdsMap(orgId);
      orgIdInstrumentIdsMap(orgId, calendarIdsMap);
    }
  }

  public static void main(String[] args) {
    new DBCalendarAndInstrumentMigrate().start();
  }
}
