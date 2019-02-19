package io.scalecube.configuration;

import io.scalecube.configuration.api.Acknowledgment;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.CreateRepositoryRequest;
import io.scalecube.configuration.api.DeleteRequest;
import io.scalecube.configuration.api.FetchRequest;
import io.scalecube.configuration.api.FetchResponse;
import io.scalecube.configuration.api.SaveRequest;
import io.scalecube.configuration.authorization.AuthorizationService;
import io.scalecube.configuration.repository.ConfigurationDataAccess;
import io.scalecube.configuration.repository.Document;
import io.scalecube.configuration.repository.Repository;
import io.scalecube.configuration.tokens.TokenVerifier;
import io.scalecube.security.api.AccessControl;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ConfigurationServiceImpl implements ConfigurationService {

  private static final Logger logger = LoggerFactory.getLogger(ConfigurationServiceImpl.class);

  private final ConfigurationDataAccess dataAccess;
  private final AccessControl accessControl;

  private ConfigurationServiceImpl(Builder builder) {
    this.dataAccess = builder.dataAccess;
    this.accessControl = builder.accessContorl;
  }

  @Override
  public Mono<Acknowledgment> createRepository(CreateRepositoryRequest request) {
    return accessControl
        .check(request.token().toString(), "configuration/createRepository")
        .map(p -> this.dataAccess.createRepository(new Repository(p.tenant(),request.repository())))
        .thenReturn(new Acknowledgment());
  }

  @Override
  public Mono<FetchResponse> fetch(FetchRequest request) {
    return accessControl
        .check(request.token().toString(), "configuration/fetch")
        .map(p -> this.dataAccess.fetch(p.tenant(), request.repository(), request.key()))
        .flatMap(
            document ->
                document
                    .map(Document::value)
                    .map(value -> new FetchResponse(request.key(), value)));
  }

  @Override
  public Flux<FetchResponse> entries(FetchRequest request) {
    return accessControl
        .check(request.token().toString(), "configuration/entries")
        .map(p -> this.dataAccess.fetchAll(p.tenant(), request.repository()))
        .flatMapMany(fluxDoc -> fluxDoc.map(doc -> new FetchResponse(doc.key(), doc.value())));
  }

  @Override
  public Mono<Acknowledgment> save(SaveRequest request) {
    
    return accessControl
        .check(request.token().toString(), "configuration/save")
        .map(p -> this.dataAccess.save( p.tenant(), request.repository() ,Document.builder()
            .id(UUID.randomUUID().toString())
            .key(request.key())
            .value(request.value())
            .build()))
        .thenReturn(new Acknowledgment());
    
  }

  @Override
  public Mono<Acknowledgment> delete(DeleteRequest request) {
    return accessControl
        .check(request.token().toString(), "configuration/delete")
        .map(p -> this.dataAccess.delete( p.tenant(), request.repository() ,request.key()))
        .thenReturn(new Acknowledgment());
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Service builder class.
   */
  public static class Builder {

    public AuthorizationService authService = AuthorizationService.builder().build();
    private ConfigurationDataAccess dataAccess;
    private AccessControl accessContorl;

    public Builder dataAccess(ConfigurationDataAccess dataAccess) {
      this.dataAccess = dataAccess;
      return this;
    }

    public Builder accessControl(AccessControl accessContorl) {
      this.accessContorl = accessContorl;
      return this;
    }
    
    /**
     * Constructs a ConfigurationService object.
     *
     * @return a instance of ConfigurationService
     */
    public ConfigurationService build() {
      return new ConfigurationServiceImpl(this);
    }
  }
}
