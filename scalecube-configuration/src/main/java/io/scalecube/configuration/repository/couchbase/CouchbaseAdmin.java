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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CouchbaseAdmin extends CouchbaseOperations {
  private static final Logger logger = LoggerFactory.getLogger(CouchbaseAdmin.class);

  private static final String CREATE_PRIMARY_INDEX =
      "CREATE PRIMARY INDEX `%s-primary-idx` ON `%s`";

  private final Cluster cluster;

  public CouchbaseAdmin(CouchbaseSettings settings, Cluster cluster) {
    super(settings);
    this.cluster = cluster;
  }

  protected boolean isBucketExists(String name) {
    logger.debug("isBucketExists: enter: name: {}", name);
    boolean bucketExists = execute(
        () ->
            cluster
                .clusterManager()
                .getBuckets()
                .stream()
                .anyMatch(bucketSettings -> Objects.equals(bucketSettings.name(), name)));
    logger.debug("isBucketExists: exit: name: {}, return: {}", name, bucketExists);
    return bucketExists;
  }

  protected void createBucket(String name) {
    execute(
        () -> {
          logger.debug("createBucket: enter: name: {}", name);
          BucketSettings bucketSettings = insertBucket(name);
          try {
            createPrimaryIndex(name);
            insertUser(name);
          } catch (Throwable throwable) {
            logger.error("createBucket: name: {}, error: {}",
                name,
                throwable);
            // rollback
            cluster.clusterManager().removeBucket(name);
            throw throwable;
          }
          logger.debug("createBucket: exit: name: {}, return bucket settings: {}",
              name,
              bucketSettings);
          return bucketSettings;
        });
  }

  private BucketSettings insertBucket(String name) {
    logger.debug("insertBucket: enter: name: {}", name);
    BucketSettings bucketSettings = cluster
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

    logger.debug("insertBucket: exit: name: {}, return: {}", name, bucketSettings);
    return bucketSettings;
  }

  private void createPrimaryIndex(String name) {
    logger.debug("createPrimaryIndext: enter: name: {}", name);
    N1qlQuery index = N1qlQuery.simple(String.format(CREATE_PRIMARY_INDEX, name, name));
    N1qlQueryResult queryResult = cluster.openBucket(name).query(index);

    if (!queryResult.finalSuccess()) {
      StringBuilder buffer = new StringBuilder();
      for (JsonObject error : queryResult.errors()) {
        buffer.append(error);
      }
      logger.error("createPrimaryIndext: name: {}, error: {}", name, buffer.toString());
      throw new CreatePrimaryIndexException(buffer.toString());
    }
    logger.debug("createPrimaryIndext: exit: name: {}", name);
  }

  private void insertUser(String name) {
    logger.debug("insetUser: enter: name: {}", name);
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
    logger.debug("insetUser: exit: name: {}", name);
  }
}
