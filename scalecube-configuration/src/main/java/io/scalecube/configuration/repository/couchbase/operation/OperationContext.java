package io.scalecube.configuration.repository.couchbase.operation;

import com.couchbase.client.java.Cluster;
import io.scalecube.configuration.repository.Document;
import io.scalecube.configuration.repository.Repository;
import io.scalecube.configuration.repository.RepositoryEntryKey;
import io.scalecube.configuration.repository.couchbase.CouchbaseSettings;

public final class OperationContext {
  private final CouchbaseSettings settings;
  private final Cluster cluster;
  private final Document document;
  private final RepositoryEntryKey key;
  private final Repository repository;

  private OperationContext(CouchbaseSettings settings, Cluster cluster,
      Document document, RepositoryEntryKey key, Repository repository) {
    this.settings = settings;
    this.cluster = cluster;
    this.document = document;
    this.key = key;
    this.repository = repository;
  }



  public static Builder builder() {
    return new Builder();
  }

  public CouchbaseSettings settings() {
    return settings;
  }

  public Cluster cluster() {
    return cluster;
  }

  public Document document() {
    return document;
  }

  public RepositoryEntryKey key() {
    return key;
  }

  public Repository repository() {
    return repository;
  }


  public static class Builder {
    private CouchbaseSettings settings;
    private Cluster cluster;
    private Document document;
    private RepositoryEntryKey key;
    private Repository repository;

    public Builder settings(CouchbaseSettings settings) {
      this.settings = settings;
      return this;
    }

    public Builder cluster(Cluster cluster) {
      this.cluster = cluster;
      return this;
    }

    public Builder document(Document document) {
      this.document = document;
      return this;
    }

    public Builder key(RepositoryEntryKey key) {
      this.key = key;
      return this;
    }

    public Builder repository(Repository repository) {
      this.repository = repository;
      return this;
    }


    public OperationContext build() {
      return new OperationContext(settings, cluster,
          document, key, repository);
    }

  }
}
