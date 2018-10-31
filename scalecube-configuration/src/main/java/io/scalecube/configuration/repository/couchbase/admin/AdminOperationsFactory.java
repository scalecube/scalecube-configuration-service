package io.scalecube.configuration.repository.couchbase.admin;

import com.couchbase.client.java.cluster.BucketSettings;
import com.couchbase.client.java.query.N1qlQueryResult;
import java.util.List;

/**
 * Factory class for constructing admin operation classes.
 */
public abstract class AdminOperationsFactory {
  public static Operation<BucketSettings> insertBucket() {
    return new InsertBucketOperation();
  }

  public static Operation<N1qlQueryResult> createPrimaryIndex() {
    return new CreatePrimaryIndexOperation();
  }

  public static Operation<Boolean> insertUser() {
    return new InsertUserOperation();
  }

  public static Operation<List<String>> getBucketNames() {
    return new ListBucketNamesOperation();
  }
}
