package io.scalecube.configuration.repository.couchbase;

import com.couchbase.client.java.Cluster;
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


public class CouchbaseDataAccess extends CouchbaseOperations implements ConfigurationDataAccess {

  private final Cluster cluster;
  private final CouchbaseAdmin couchbaseAdmin;

  /**
   * Create couchbase data access operations instance.
   *
   * @param settings couchbase settings
   * @param cluster couchbase cluster
   * @param couchbaseAdmin couchbase operations with admin permissions
   */
  public CouchbaseDataAccess(
      CouchbaseSettings settings, Cluster cluster, CouchbaseAdmin couchbaseAdmin) {
    super(settings);
    this.cluster = cluster;
    this.couchbaseAdmin = couchbaseAdmin;
  }

  @Override
  public Mono<Void> createRepository(Repository repository) {
    return execute(() -> createRepository(context(repository)));
  }

  @Override
  public Mono<Document> get(RepositoryEntryKey key) {
    return execute(() -> execute(key, null, OperationType.Read));
  }

  @Override
  public Mono<Document> put(RepositoryEntryKey key, Document document) {
    return execute(() -> execute(key, document, OperationType.Write));
  }

  @Override
  public Mono<String> remove(RepositoryEntryKey key) {
    return execute(() -> execute(key, null, OperationType.Delete).id());
  }

  @Override
  public Flux<Document> entries(Repository repository) {
    return EntryOperation.getOperation(OperationType.List).execute(context(repository));
  }

  private boolean createRepository(OperationContext context) {
    CreateRepositoryOperation operation = new CreateRepositoryOperation();
    return operation.execute(couchbaseAdmin, context);
  }

  private Flux<Document> execute(RepositoryEntryKey key, Document document, OperationType type) {
    return EntryOperation
        .getOperation(type)
        .execute(context(key, document));
  }

  private OperationContext context(RepositoryEntryKey key, Document document) {
    return context()
        .document(document)
        .key(key)
        .build();
  }

  private OperationContext context(Repository repository) {
    return context()
        .repository(repository)
        .build();
  }

  private OperationContext.Builder context() {
    return OperationContext.builder()
        .cluster(cluster)
        .settings(settings);
  }
}
