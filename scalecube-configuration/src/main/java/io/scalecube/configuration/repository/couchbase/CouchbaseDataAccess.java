package io.scalecube.configuration.repository.couchbase;

import com.couchbase.client.java.AsyncCluster;
import io.scalecube.configuration.repository.ConfigurationDataAccess;
import io.scalecube.configuration.repository.Document;
import io.scalecube.configuration.repository.Repository;
import io.scalecube.configuration.repository.RepositoryEntryKey;
import io.scalecube.configuration.repository.couchbase.operation.CreateRepositoryOperation;
import io.scalecube.configuration.repository.couchbase.operation.EntryOperation;
import io.scalecube.configuration.repository.couchbase.operation.EntryOperation.OperationType;
import io.scalecube.configuration.repository.couchbase.operation.OperationContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CouchbaseDataAccess implements ConfigurationDataAccess {

  private final CouchbaseSettings settings;
  private final AsyncCluster cluster;
  private final CouchbaseAdmin couchbaseAdmin;

  /**
   * Create couchbase data access operations instance.
   *
   * @param settings couchbase settings
   * @param cluster couchbase cluster
   * @param couchbaseAdmin couchbase operations with admin permissions
   */
  public CouchbaseDataAccess(
      CouchbaseSettings settings, AsyncCluster cluster, CouchbaseAdmin couchbaseAdmin) {
    this.settings = settings;
    this.cluster = cluster;
    this.couchbaseAdmin = couchbaseAdmin;
  }

  @Override
  public Mono<Boolean> createRepository(Repository repository) {
    return new CreateRepositoryOperation().execute(couchbaseAdmin, context(repository));
  }

  @Override
  public Mono<Document> fetch(String tenant, String repository, String key) {
    return get(
        RepositoryEntryKey.builder()
            .repository(new Repository(tenant, repository))
            .key(key)
            .build());
  }

  @Override
  public Flux<Document> fetchAll(String tenant, String repository) {
    return entries(new Repository(tenant, repository));
  }
  
  @Override
  public Mono<Document> save(String tenant, String repository, Document document) {
    return put(
        RepositoryEntryKey.builder().repository(new Repository(tenant, repository)).build(), document);
  }

  @Override
  public Mono<String> delete(String tenant, String repository, String key) {
    return remove(RepositoryEntryKey.builder().repository(new Repository(tenant, repository)).build());
  }
  
  private Mono<Document> get(RepositoryEntryKey key) {
    EntryOperation<Mono<Document>> operation = EntryOperation.getOperation(OperationType.Read);
    return operation.execute(context(key, null));
  }
  
  private Mono<Document> put(RepositoryEntryKey key, Document document) {
    EntryOperation<Mono<Document>> operation = EntryOperation.getOperation(OperationType.Write);
    return operation.execute(context(key, document));
  }

  private Mono<String> remove(RepositoryEntryKey key) {
    EntryOperation<Mono<Document>> operation = EntryOperation.getOperation(OperationType.Delete);
    return operation.execute(context(key, null)).map(Document::id);
  }

  private Flux<Document> entries(Repository repository) {
    EntryOperation<Flux<Document>> operation = EntryOperation.getOperation(OperationType.List);
    return operation.execute(context(repository));
  }

  private OperationContext context(RepositoryEntryKey key, Document document) {
    return context().document(document).key(key).build();
  }

  private OperationContext context(Repository repository) {
    return context().repository(repository).build();
  }

  private OperationContext.Builder context() {
    return OperationContext.builder().cluster(cluster).settings(settings);
  }

 
  
}
