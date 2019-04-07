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
  private final String docName;

  private AdminOperationContext(Builder builder) {
    this.settings = builder.settings;
    this.cluster = builder.cluster;
    this.name = builder.name;
    this.docName = builder.docName;
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

  public String name() {
    return name;
  }

  public String docName() {
    return docName;
  }

  public static class Builder {

    private CouchbaseSettings settings;
    private AsyncCluster cluster;
    private String name;
    private String docName;

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

    public Builder docName(String docName) {
      this.docName = docName;
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
      Objects.requireNonNull(docName, "docName");
      return new AdminOperationContext(this);
    }
  }
}
