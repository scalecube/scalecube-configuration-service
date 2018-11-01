package io.scalecube.configuration.repository.couchbase;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.cluster.BucketSettings;
import io.scalecube.configuration.repository.couchbase.admin.AdminOperationContext;
import io.scalecube.configuration.repository.couchbase.admin.AdminOperationsFactory;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a class of couchbase admin operations.
 */
public class CouchbaseAdmin extends CouchbaseOperations {
  private static final Logger logger = LoggerFactory.getLogger(CouchbaseAdmin.class);
  private final Cluster cluster;

  /**
   * Constructs a {@link CouchbaseAdmin} using the given arguments.
   * @param settings app settings
   * @param cluster Couchbase cluster
   */
  public CouchbaseAdmin(CouchbaseSettings settings, Cluster cluster) {
    super(settings);
    this.cluster = cluster;
  }


  /**
   * Returns true if a bucket matching to the name argument exists.
   * @param name candidate for test
   * @return true if bucket by the name argument exists; false otherwise
   */
  public boolean isBucketExists(String name) {
    logger.debug("isBucketExists: enter: name: {}", name);
    boolean bucketExists = execute(
        () -> AdminOperationsFactory
            .getBucketNames()
            .execute(operationContext(name))
            .stream()
            .anyMatch(bucketName -> Objects.equals(bucketName, name)));
    logger.debug("isBucketExists: exit: name: {}, return: {}", name, bucketExists);
    return bucketExists;
  }

  /**
   * Creates a bucket with the name argument.
   * @param name the bucket name
   */
  public void createBucket(String name) {
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
    BucketSettings bucketSettings = AdminOperationsFactory
        .insertBucket()
        .execute(operationContext(name));
    logger.debug("insertBucket: exit: name: {}, return: {}", name, bucketSettings);
    return bucketSettings;
  }



  private void createPrimaryIndex(String name) {
    logger.debug("createPrimaryIndex: enter: name: {}", name);
    AdminOperationsFactory.createPrimaryIndex().execute(operationContext(name));
    logger.debug("createPrimaryIndex: exit: name: {}", name);
  }



  private void insertUser(String name) {
    logger.debug("insetUser: enter: name: {}", name);
    AdminOperationsFactory.insertUser().execute(operationContext(name));
    logger.debug("insetUser: exit: name: {}", name);
  }

  private AdminOperationContext operationContext(String name)  {
    return AdminOperationContext
        .builder()
        .settings(settings)
        .cluster(cluster)
        .name(name)
        .build();
  }
}
