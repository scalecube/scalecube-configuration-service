package io.scalecube.configuration.repository.couchbase;

import com.couchbase.client.java.bucket.BucketType;
import java.util.Arrays;
import java.util.List;

public final class CouchbaseSettings {

  public static final String BUCKET_NAME_FORMAT = "%s-%s";
  public static final List<String> BUCKET_ROLES =
      Arrays.asList("data_reader", "data_writer", "query_select");
  public static final String BUCKET_TYPE = "COUCHBASE";
  public static final int BUCKET_QUOTA = 100;
  public static final int BUCKET_REPLICAS = 0;
  public static final boolean BUCKET_INDEX_REPLICAS = false;
  public static final boolean BUCKET_ENABLE_FLUSH = false;

  private List<String> hosts;
  private String username;
  private String password;
  private String bucketNamePattern = BUCKET_NAME_FORMAT;
  private List<String> bucketRoles = BUCKET_ROLES;
  private String bucketType = BUCKET_TYPE;
  private int bucketQuota = BUCKET_QUOTA; // quota in megabytes
  private int bucketReplicas = BUCKET_REPLICAS;
  private boolean bucketIndexReplicas = BUCKET_INDEX_REPLICAS;
  private boolean bucketEnableFlush = BUCKET_ENABLE_FLUSH;

  public List<String> hosts() {
    return hosts;
  }

  public String username() {
    return username;
  }

  public String password() {
    return password;
  }

  public String bucketNamePattern() {
    return bucketNamePattern;
  }

  public List<String> bucketRoles() {
    return bucketRoles;
  }

  public BucketType bucketType() {
    return BucketType.valueOf(bucketType);
  }

  public int bucketQuota() {
    return bucketQuota;
  }

  public int bucketReplicas() {
    return bucketReplicas;
  }

  public boolean bucketIndexReplicas() {
    return bucketIndexReplicas;
  }

  public boolean bucketEnableFlush() {
    return bucketEnableFlush;
  }
}
