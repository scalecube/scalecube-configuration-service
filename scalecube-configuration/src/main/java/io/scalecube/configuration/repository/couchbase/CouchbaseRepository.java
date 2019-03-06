package io.scalecube.configuration.repository.couchbase;

import com.couchbase.client.java.AsyncCluster;
import io.scalecube.configuration.repository.ConfigurationRepository;
import io.scalecube.configuration.repository.Document;
import io.scalecube.configuration.repository.Repository;
import io.scalecube.configuration.repository.RepositoryEntryKey;
import io.scalecube.configuration.repository.couchbase.operation.CreateRepositoryOperation;
import io.scalecube.configuration.repository.couchbase.operation.EntryOperation;
import io.scalecube.configuration.repository.couchbase.operation.EntryOperation.OperationType;
import io.scalecube.configuration.repository.couchbase.operation.OperationContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CouchbaseRepository implements ConfigurationRepository {

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
  public CouchbaseRepository(
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
    EntryOperation<Mono<Document>> operation = EntryOperation.getOperation(OperationType.Read);
    return operation.execute(
        context(
            RepositoryEntryKey.builder()
                .repository(new Repository(tenant, repository))
                .key(key)
                .build(),
            null));
  }

  @Override
  public Flux<Document> fetchAll(String tenant, String repository) {
    EntryOperation<Flux<Document>> operation = EntryOperation.getOperation(OperationType.List);
    return operation.execute(context(new Repository(tenant, repository)));
  }

  @Override
  public Mono<Document> save(String tenant, String repository, Document document) {
    EntryOperation<Mono<Document>> operation = EntryOperation.getOperation(OperationType.Write);
    return operation.execute(
        context(
            RepositoryEntryKey.builder()
                .repository(new Repository(tenant, repository))
                .key(document.key())
                .build(),
            document));
  }

  @Override
  public Mono<String> delete(String tenant, String repository, String key) {
    EntryOperation<Mono<Document>> operation = EntryOperation.getOperation(OperationType.Delete);
    return operation
        .execute(
            context(
                RepositoryEntryKey.builder()
                    .repository(new Repository(tenant, repository))
                    .key(key)
                    .build(),
                null))
        .map(Document::id);
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
