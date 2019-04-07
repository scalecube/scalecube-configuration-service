package io.scalecube.configuration.repository.couchbase.admin;

import com.couchbase.client.java.cluster.BucketSettings;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Factory class for constructing admin operation classes.
 */
public abstract class AdminOperationsFactory {

  public static Operation<Mono<BucketSettings>> insertBucket() {
    return new InsertBucketOperation();
  }

  public static Operation<Mono<Boolean>> insertDoc() {
    return new InsertDocOperation();
  }

  public static Operation<Mono<Boolean>> createPrimaryIndex() {
    return new CreatePrimaryIndexOperation();
  }

  public static Operation<Mono<Boolean>> insertUser() {
    return new InsertUserOperation();
  }

  public static Operation<Flux<String>> getBucketNames() {
    return new ListBucketNamesOperation();
  }
}
