package io.scalecube.configuration;

import static java.util.Objects.requireNonNull;

import io.scalecube.cluster.membership.IdGenerator;
import io.scalecube.configuration.api.AccessRequest;
import io.scalecube.configuration.api.Acknowledgment;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.CreateRepositoryRequest;
import io.scalecube.configuration.api.DeleteRequest;
import io.scalecube.configuration.api.FetchRequest;
import io.scalecube.configuration.api.FetchResponse;
import io.scalecube.configuration.api.SaveRequest;
import io.scalecube.configuration.repository.ConfigurationDataAccess;
import io.scalecube.configuration.repository.Document;
import io.scalecube.configuration.repository.Repository;
import io.scalecube.security.api.AccessControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ConfigurationServiceImpl implements ConfigurationService {

  private static final Logger logger = LoggerFactory.getLogger(ConfigurationServiceImpl.class);

  private final ConfigurationDataAccess dataAccess;
  private final AccessControl accessControl;

  public ConfigurationServiceImpl(ConfigurationDataAccess dataAccess, AccessControl accessContorl) {
    this.dataAccess = dataAccess;
    this.accessControl = accessContorl;
  }

  @Override
  public Mono<Acknowledgment> createRepository(CreateRepositoryRequest request) {
    requireNonNullAccessRequest(request);
    return accessControl
        .check(request.token().toString(), ConfigurationService.CONFIG_CREATE_REPO)
        .flatMap(
            p -> this.dataAccess.createRepository(new Repository(p.tenant(), request.repository())))
        .map(b->new Acknowledgment());
  }

  @Override
  public Mono<FetchResponse> fetch(FetchRequest request) {
    requireNonNullAccessRequest(request);
    return accessControl
        .check(request.token().toString(), ConfigurationService.CONFIG_FETCH)
        .map(p -> this.dataAccess.fetch(p.tenant(), request.repository(), request.key()))
        .flatMap(
            document ->
                document
                    .map(Document::value)
                    .map(value -> new FetchResponse(request.key(), value)));
  }

  @Override
  public Flux<FetchResponse> entries(FetchRequest request) {
    requireNonNullAccessRequest(request);
    return accessControl
        .check(request.token().toString(), ConfigurationService.CONFIG_ENTRIES)
        .map(p -> this.dataAccess.fetchAll(p.tenant(), request.repository()))
        .flatMapMany(fluxDoc -> fluxDoc.map(doc -> new FetchResponse(doc.key(), doc.value())));
  }

  @Override
  public Mono<Acknowledgment> save(SaveRequest request) {
    requireNonNullAccessRequest(request);
    return accessControl
        .check(request.token().toString(), ConfigurationService.CONFIG_SAVE)
        .map(
            p ->
                this.dataAccess.save(
                    p.tenant(),
                    request.repository(),
                    Document.builder()
                        .id(IdGenerator.generateId())
                        .key(request.key())
                        .value(request.value())
                        .build()))
        .thenReturn(new Acknowledgment());
  }

  @Override
  public Mono<Acknowledgment> delete(DeleteRequest request) {
    requireNonNullAccessRequest(request);
    return accessControl
        .check(request.token().toString(), ConfigurationService.CONFIG_DELETE)
        .map(p -> this.dataAccess.delete(p.tenant(), request.repository(), request.key()))
        .thenReturn(new Acknowledgment());
  }
  
  private static void requireNonNullAccessRequest(AccessRequest request) {
    requireNonNull(request);
    requireNonNull(request.repository());
    requireNonNull(request.token()); 
  }
}
