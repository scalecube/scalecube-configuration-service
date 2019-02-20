package io.scalecube.configuration.repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** An abstraction of configuration data access. */
public interface ConfigurationRepository {

  /**
   * Creates a name in underlying data source.
   *
   * @param repository Repository settings
   * @return True if name created; false otherwise.
   */
  Mono<Boolean> createRepository(Repository repository);

  Mono<Document> fetch(String tenant, String repository, String key);

  Flux<Document> fetchAll(String tenant, String repository);

  Mono<Document> save(String tenant, String repository, Document build);

  Mono<String> delete(String tenant, String repository, String key);
}
