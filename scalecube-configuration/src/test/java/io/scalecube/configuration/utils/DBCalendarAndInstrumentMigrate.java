package io.scalecube.configuration.utils;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.JsonArrayDocument;
import com.couchbase.client.java.query.N1qlParams;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.Query;

public class DBCalendarAndInstrumentMigrate {

  private static final String DEFAULT_HOST = "localhost";
  private static final String DEFAULT_PORT = "8091";
  private static final String DEFAULT_USER = "admin";
  private static final String DEFAULT_PASSWORD = "123456";
  private static final String DEFAULT_BUCKET_NAME = "configurations";

  private final String host;
  private final String port;
  private final String user;
  private final String password;
  private final String bucketName;

  private Bucket bucket;
  //Calendar and its instrument
  private Map<String, String> instrumentCalendarMap = new HashMap<>();

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

  private void start() {
    bucket = getBucket();

    List repoContent = bucket.get("repos", JsonArrayDocument.class).content().toList();


//    bucket.query(N1qlQuery.simple())
//
//    N1qlParams params = N1qlParams.build().adhoc(false);
//    N1qlQuery query = N1qlQuery.simple("select count(*) from `configurations`", params);
//    System.out.println(query.);

//    N1qlQueryResult queryResult =
//        bucket.query(Query.simple("SELECT * FROM beer-sample LIMIT 10"));

    System.out.println("Simple string query:");
//    N1qlQuery airlineQuery = N1qlQuery.simple("select count(*) from `configurations`");
    N1qlQuery airlineQuery = N1qlQuery.simple("select * from configurations limit 20");
    N1qlQueryResult queryResult = bucket.query(airlineQuery);

    for (N1qlQueryRow result: queryResult) {
      System.out.println(result.value());
    }

//    System.exit(0);

    repoContent.forEach(
        el -> {
          String e = (String) el;
          if (e.endsWith("CalendarsList")) {
            instrumentCalendarMap.put(
                e,
                (String)
                    repoContent.get(
                        repoContent.indexOf(e.split("::")[0] + "::" + "InstrumentsList")));
          }
        });

    System.out.println(repoContent);
    System.out.println(instrumentCalendarMap);
  }

  public static void main(String[] args) {
    new DBCalendarAndInstrumentMigrate().start();
  }
}
