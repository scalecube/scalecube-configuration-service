package io.scalecube.configuration.repository.couchbase;

import io.scalecube.configuration.repository.exception.DataAccessResourceFailureException;

import com.couchbase.client.java.bucket.BucketType;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

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
  private final Properties settings;
  private List<String> clusterNodes;
  private List<String> roles;

  private CouchbaseSettings() {
    settings = new Properties();

    try {
      settings.load(getClass().getResourceAsStream("/couchbase-settings.properties"));
    } catch (IOException ex) {
      throw new DataAccessResourceFailureException("Failed to initialize", ex);
    }
  }

  BucketType bucketType() {
    return Enum.valueOf(BucketType.class, getProperty(BUCKET_TYPE));
  }

  int bucketQuota() {
    return Integer.valueOf(getProperty(BUCKET_QUOTA));
  }

  int bucketReplicas() {
    return Integer.valueOf(getProperty(BUCKET_REPLICAS));
  }

  boolean bucketIndexReplicas() {
    return Boolean.valueOf(getProperty(BUCKET_INDEX_REPLICAS));
  }

  boolean bucketEnableFlush() {
    return Boolean.valueOf(getProperty(BUCKET_ENABLE_FLUSH));
  }

  String couchbaseAdmin() {
    return getProperty(COUCHBASE_ADMIN);
  }

  String couchbaseAdminPassword() {
    return getProperty(COUCHBASE_ADMIN_PASSWORD);
  }

  List<String> couchbaseClusterNodes() {
    clusterNodes = getList(COUCHBASE_CLUSTER_NODES, clusterNodes);
    return clusterNodes;
  }

  List<String> bucketsRoles() {
    roles = getList(BUCKET_ROLES, roles);
    return roles;
  }

  private List<String> getList(String key, List<String> list) {
    if (list == null) {
      String value = getProperty(key);
      list = value.length() > 0 ? Arrays.asList(value.split(",")) : new ArrayList<>();
    }
    return list;
  }

  String bucketNamePattern() {
    return getProperty(NEW_BUCKET_NAME_FORMAT);
  }


  private String getProperty(String key) {
    return settings.getProperty(key);
  }

  static class Builder {

    public CouchbaseSettings build() {
      return new CouchbaseSettings();
    }
  }
}
