package io.scalecube.configuration.repository.couchbase.admin;

import com.couchbase.client.java.AsyncCluster;
import io.scalecube.configuration.repository.couchbase.CouchbaseSettings;
import java.util.Objects;

/**
 * Represents a data structure used to execute an admin operation.
 */
public final class AdminOperationContext {

  private final CouchbaseSettings settings;
  private final AsyncCluster cluster;
  private final String name;
  private final String orgId;
  private final String repoName;

  private AdminOperationContext(Builder builder) {
    this.settings = builder.settings;
    this.cluster = builder.cluster;
    this.name = builder.name;
    this.orgId = builder.orgId;
    this.repoName = builder.repoName;
  }

  public static Builder builder() {
    return new Builder();
  }

  public CouchbaseSettings settings() {
    return settings;
  }

  public AsyncCluster cluster() {
    return cluster;
  }

  /**
   * Bucket name.
   * @return bucket name
   */
  public String name() {
    return name;
  }

  public String orgId() {
    return orgId;
  }

  public String repoName() {
    return repoName;
  }

  public static class Builder {

    private CouchbaseSettings settings;
    private AsyncCluster cluster;
    private String name;
    private String orgId;
    private String repoName;

    public Builder settings(CouchbaseSettings settings) {
      this.settings = settings;
      return this;
    }

    public Builder cluster(AsyncCluster cluster) {
      this.cluster = cluster;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder orgId(String orgId) {
      this.orgId = orgId;
      return this;
    }

    public Builder repoName(String repoName) {
      this.repoName = repoName;
      return this;
    }

    /**
     * Constructs an instance of {@link AdminOperationContext} using this builder fields.
     *
     * @return an instance of {@link AdminOperationContext}
     */
    public AdminOperationContext build() {
      Objects.requireNonNull(settings, "settings");
      Objects.requireNonNull(cluster, "cluster");
      Objects.requireNonNull(name, "name");
      return new AdminOperationContext(this);
    }
  }
}
