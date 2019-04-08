package io.scalecube.configuration.repository.couchbase;

import com.couchbase.client.java.AsyncCluster;
import com.couchbase.client.java.cluster.BucketSettings;
import io.scalecube.configuration.repository.couchbase.admin.AdminOperationContext;
import io.scalecube.configuration.repository.couchbase.admin.AdminOperationsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import rx.RxReactiveStreams;

/**
 * Represents a class of couchbase admin operations.
 */
public final class CouchbaseAdmin {

  private static final Logger logger = LoggerFactory.getLogger(CouchbaseAdmin.class);

  private final CouchbaseSettings settings;
  private final AsyncCluster cluster;

  /**
   * Constructs a {@link CouchbaseAdmin} using the given arguments.
   *
   * @param settings app settings
   * @param cluster Couchbase cluster
   */
  public CouchbaseAdmin(CouchbaseSettings settings, AsyncCluster cluster) {
    this.settings = settings;
    this.cluster = cluster;
  }

  /**
   * Returns true if a bucket matching to the name argument exists.
   *
   * @param name candidate for test
   * @return true if bucket by the name argument exists; false otherwise
   */
  public Mono<Boolean> isBucketExists(String name) {
    return Mono.fromRunnable(() -> logger.debug("isBucketExists: enter: name: {}", name))
        .then(
            AdminOperationsFactory.getBucketNames()
                .execute(operationContext(name))
                .filter(bucketName -> bucketName.equals(name))
                .hasElements())
        .onErrorMap(CouchbaseExceptionTranslator::translateExceptionIfPossible)
        .doOnError(th -> logger.error("isBucketExists: name: {}, error: {}", name, th))
        .doOnSuccess(
            exists -> logger.debug("isBucketExists: exit: name: {}, return: {}", name, exists));
  }

  /**
   * Creates a bucket with the name argument.
   *
   * @param name the bucket name
   */
  public Mono<Boolean> createBucket(String name) {
    return Mono.fromRunnable(() -> logger.debug("createBucket: enter: name: {}", name))
        .then(insertBucket(name))
        .then(createPrimaryIndex(name))
        .then(insertUser(name))
        .onErrorResume(
            th ->
                Mono.from(
                    RxReactiveStreams.toPublisher(
                        cluster
                            .clusterManager()
                            .map(
                                asyncClusterManager -> asyncClusterManager.removeBucket(name))))
                    .then(Mono.error(th)))
        .onErrorMap(CouchbaseExceptionTranslator::translateExceptionIfPossible)
        .doOnError(th -> logger.error("createBucket: name: {}, error: {}", name, th))
        .doOnSuccess(result -> logger.debug("createBucket: exit: name: {}", name));
  }

  /**
   * Insert document in config bucket.
   *
   * @param bucketName config bucket name
   * @param orgId organization id
   * @param repoName repository name
   */
  public Mono<Boolean> insertDoc(String bucketName, String orgId, String repoName) {
    return Mono.fromRunnable(() -> logger
        .debug("insert doc: enter: bucket name: {}; organization id: {}; repo name: {}", bucketName,
            orgId, repoName))
        .then(AdminOperationsFactory.insertDoc()
            .execute(operationContext(bucketName, orgId, repoName)))
        .doOnSuccess(
            settings -> logger
                .debug("insert doc: exit: bucket name: {}; organization id: {}; "
                    + "repo name: {} return: {}", bucketName, orgId, repoName, settings));
  }

  private Mono<BucketSettings> insertBucket(String name) {
    return Mono.fromRunnable(() -> logger.debug("insertBucket: enter: name: {}", name))
        .then(AdminOperationsFactory.insertBucket().execute(operationContext(name)))
        .doOnSuccess(
            settings -> logger.debug("insertBucket: exit: name: {}, return: {}", name, settings));
  }

  private Mono<Boolean> createPrimaryIndex(String name) {
    return Mono.fromRunnable(() -> logger.debug("createPrimaryIndex: enter: name: {}", name))
        .then(AdminOperationsFactory.createPrimaryIndex().execute(operationContext(name)))
        .doOnSuccess(
            indexCreated ->
                logger.debug("createPrimaryIndex: exit: name: {}, return: {}", name, indexCreated));
  }

  private Mono<Boolean> insertUser(String name) {
    return Mono.fromRunnable(() -> logger.debug("insetUser: enter: name: {}", name))
        .then(AdminOperationsFactory.insertUser().execute(operationContext(name)))
        .doOnSuccess(
            userCreated -> logger.debug("insetUser: exit: name: {}, return {}", name, userCreated));
  }

  private AdminOperationContext operationContext(String name) {
    return AdminOperationContext.builder().settings(settings).cluster(cluster).name(name).build();
  }

  private AdminOperationContext operationContext(String bucketName, String orgId, String repoName) {
    return AdminOperationContext.builder().settings(settings).cluster(cluster).name(bucketName)
        .orgId(orgId).repoName(repoName).build();
  }
}
