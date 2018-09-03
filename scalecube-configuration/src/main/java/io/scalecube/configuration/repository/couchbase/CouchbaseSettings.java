package io.scalecube.configuration.repository.couchbase;

import com.couchbase.client.java.bucket.BucketType;
import io.scalecube.config.ConfigRegistry;
import io.scalecube.configuration.ConfigRegistryConfiguration;
import java.util.Collections;
import java.util.List;

final class CouchbaseSettings {

  private static final String COUCHBASE_ADMIN = "couchbase.admin.user";
  private static final String COUCHBASE_ADMIN_PASSWORD = "couchbase.admin.password";
  private static final String COUCHBASE_CLUSTER_NODES = "couchbase.cluster.nodes";
  private static final String NEW_BUCKET_NAME_FORMAT = "bucket.name.format";
  private static final String BUCKET_TYPE = "bucket.type";
  private static final String BUCKET_QUOTA = "bucket.quota";
  private static final String BUCKET_ROLES = "bucket.roles";
  private static final String BUCKET_REPLICAS = "bucket.replicas";
  private static final String BUCKET_INDEX_REPLICAS =
      "bucket.indexReplicas";
  private static final String BUCKET_ENABLE_FLUSH = "bucket.enableFlush";
  private final CouchbaseProperties couchbaseProperties;
  private List<String> clusterNodes;
  private List<String> roles;

  private ConfigRegistry configRegistry = ConfigRegistryConfiguration.configRegistry();

  public static void main(String[] args) {
    ConfigRegistry configRegistry = ConfigRegistryConfiguration.configRegistry();
    configRegistry.allProperties();
  }

  private CouchbaseSettings() {
    couchbaseProperties = configRegistry
        .objectProperty("couchbase", CouchbaseProperties.class).value().get();
  }

  BucketType bucketType() {
    return Enum.valueOf(BucketType.class, configRegistry.stringValue(BUCKET_TYPE, null));
  }

  int bucketQuota() {
    return configRegistry.intValue(BUCKET_QUOTA, 0);
  }

  int bucketReplicas() {
    return configRegistry.intValue(BUCKET_REPLICAS, 1);
  }

  boolean bucketIndexReplicas() {
    return configRegistry.booleanValue(BUCKET_INDEX_REPLICAS, false);
  }

  boolean bucketEnableFlush() {
    return configRegistry.booleanValue(BUCKET_ENABLE_FLUSH, false);
  }

  String couchbaseAdmin() {
    return couchbaseProperties.username();
  }

  String couchbaseAdminPassword() {
    return couchbaseProperties.password();
  }

  List<String> couchbaseClusterNodes() {
    return couchbaseProperties.hosts() == null
        ? Collections.EMPTY_LIST
        : couchbaseProperties.hosts();
  }

  List<String> bucketsRoles() {
    return configRegistry.stringListValue(BUCKET_ROLES, Collections.emptyList());
  }

  String bucketNamePattern() {
    return configRegistry.stringValue(NEW_BUCKET_NAME_FORMAT, null);
  }

  static class Builder {

    public CouchbaseSettings build() {
      return new CouchbaseSettings();
    }
  }
}
