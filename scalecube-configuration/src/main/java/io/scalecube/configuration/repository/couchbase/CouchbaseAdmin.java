package io.scalecube.configuration.repository.couchbase;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.cluster.AuthDomain;
import com.couchbase.client.java.cluster.BucketSettings;
import com.couchbase.client.java.cluster.DefaultBucketSettings.Builder;
import com.couchbase.client.java.cluster.UserRole;
import com.couchbase.client.java.cluster.UserSettings;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import io.scalecube.configuration.repository.exception.CreatePrimaryIndexException;
import java.util.Objects;
import java.util.stream.Collectors;

public class CouchbaseAdmin extends CouchbaseOperations {

  private static final String CREATE_PRIMARY_INDEX =
      "CREATE PRIMARY INDEX `%s-primary-idx` ON `%s`";

  private final Cluster cluster;

  public CouchbaseAdmin(CouchbaseSettings settings, Cluster cluster) {
    super(settings);
    this.cluster = cluster;
  }

  protected boolean isBucketExists(String name) {
    return execute(
        () ->
            cluster
                .clusterManager()
                .getBuckets()
                .stream()
                .anyMatch(bucketSettings -> Objects.equals(bucketSettings.name(), name)));
  }

  protected void createBucket(String name) {
    execute(
        () -> {
          BucketSettings bucketSettings = insertBucket(name);
          try {
            createPrimaryIndex(name);
            insertUser(name);
          } catch (Throwable throwable) {
            // rollback
            cluster.clusterManager().removeBucket(name);
            throw throwable;
          }
          return bucketSettings;
        });
  }

  private BucketSettings insertBucket(String name) {
    return cluster
        .clusterManager()
        .insertBucket(
            new Builder()
                .type(settings.bucketType())
                .name(name)
                .quota(settings.bucketQuota()) // megabytes
                .replicas(settings.bucketReplicas())
                .indexReplicas(settings.bucketIndexReplicas())
                .enableFlush(settings.bucketEnableFlush())
                .build());
  }

  private void createPrimaryIndex(String name) {
    N1qlQuery index = N1qlQuery.simple(String.format(CREATE_PRIMARY_INDEX, name, name));
    N1qlQueryResult queryResult = cluster.openBucket(name).query(index);

    if (!queryResult.finalSuccess()) {
      StringBuilder buffer = new StringBuilder();
      for (JsonObject error : queryResult.errors()) {
        buffer.append(error);
      }
      throw new CreatePrimaryIndexException(buffer.toString());
    }
  }

  private void insertUser(String name) {
    cluster
        .clusterManager()
        .upsertUser(
            AuthDomain.LOCAL,
            name,
            UserSettings.build()
                .password(PasswordGenerator.md5Hash(name))
                .name(name)
                .roles(
                    settings
                        .bucketRoles()
                        .stream()
                        .map(role -> new UserRole(role, name))
                        .collect(Collectors.toList())));
  }
}
