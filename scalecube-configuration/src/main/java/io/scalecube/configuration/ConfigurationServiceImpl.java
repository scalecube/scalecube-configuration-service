package io.scalecube.configuration;

import io.scalecube.configuration.api.Acknowledgment;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.CreateRepositoryRequest;
import io.scalecube.configuration.api.DeleteRequest;
import io.scalecube.configuration.api.FetchRequest;
import io.scalecube.configuration.api.FetchResponse;
import io.scalecube.configuration.api.SaveRequest;
import io.scalecube.configuration.authorization.AuthorizationService;
import io.scalecube.configuration.authorization.OperationType;
import io.scalecube.configuration.operation.ServiceOperationContext;
import io.scalecube.configuration.operation.ServiceOperationFactory;
import io.scalecube.configuration.repository.ConfigurationDataAccess;
import io.scalecube.configuration.tokens.TokenVerifier;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class ConfigurationServiceImpl implements ConfigurationService {

  private static final Logger logger = LoggerFactory.getLogger(ConfigurationServiceImpl.class);

  private final ConfigurationDataAccess dataAccess;
  private TokenVerifier tokenVerifier;
  private final AuthorizationService authorizationService;

  private ConfigurationServiceImpl(
      ConfigurationDataAccess dataAccess, TokenVerifier tokenVerifier) {
    this.dataAccess = dataAccess;
    this.tokenVerifier = tokenVerifier;
    this.authorizationService = AuthorizationService.builder().build();
  }

  @Override
  public Mono<Acknowledgment> createRepository(CreateRepositoryRequest request) {
    return Mono.fromRunnable(() -> logger.debug("createRepository: enter: request: {}", request))
        .then(Mono.fromCallable(ServiceOperationFactory::createRepository))
        .flatMap(
            operation ->
                Mono.from(operation.execute(request, context(OperationType.CreateRepoitory))))
        .subscribeOn(Schedulers.parallel())
        .doOnSuccess(result -> logger.debug("createRepository: exit: request: {}", request))
        .doOnError(th -> logger.error("createRepository: request: {}, error: {}", request, th));
  }

  @Override
  public Mono<FetchResponse> fetch(FetchRequest request) {
    return Mono.fromRunnable(() -> logger.debug("fetch: enter: request: {}", request))
        .then(Mono.fromCallable(ServiceOperationFactory::fetch))
        .flatMap(operation -> Mono.from(operation.execute(request, context(OperationType.Read))))
        .subscribeOn(Schedulers.parallel())
        .doOnSuccess(
            response -> logger.debug("fetch: exit: request: {}, response: {}", request, response))
        .doOnError(th -> logger.error("fetch: request: {}, error: {}", request, th));
  }

  @Override
  public Flux<FetchResponse> entries(FetchRequest request) {
    return Mono.fromRunnable(() -> logger.debug("entries: enter: request: {}", request))
        .then(Mono.fromCallable(ServiceOperationFactory::fetchAll))
        .flatMapMany(
            operation -> Flux.from(operation.execute(request, context(OperationType.List))))
        .subscribeOn(Schedulers.parallel())
        .doOnComplete(() -> logger.debug("entries: exit: request: {}", request))
        .doOnError(th -> logger.error("entries: request: {}, error: {}", request, th));
  }

  @Override
  public Mono<List<FetchResponse>> collectEntries(FetchRequest request) {
    return entries(request).collectList();
  }

  @Override
  public Mono<Acknowledgment> save(SaveRequest request) {
    return Mono.fromRunnable(() -> logger.debug("save: enter: request: {}", request))
        .then(Mono.fromCallable(ServiceOperationFactory::saveEntry))
        .flatMap(operation -> Mono.from(operation.execute(request, context(OperationType.Write))))
        .subscribeOn(Schedulers.parallel())
        .doOnSuccess(result -> logger.debug("save: exit: request: {}", request))
        .doOnError(th -> logger.error("save: request: {}, error: {}", request, th));
  }

  @Override
  public Mono<Acknowledgment> delete(DeleteRequest request) {
    return Mono.fromRunnable(() -> logger.debug("delete: enter: request: {}", request))
        .then(Mono.fromCallable(ServiceOperationFactory::deleteEntry))
        .flatMap(operation -> Mono.from(operation.execute(request, context(OperationType.Delete))))
        .subscribeOn(Schedulers.parallel())
        .doOnSuccess(result -> logger.debug("delete: exit: request: {}", request))
        .doOnError(th -> logger.error("delete: request: {}, error: {}", request, th));
  }

  private ServiceOperationContext context(OperationType operationType) {
    return ServiceOperationContext.builder()
        .operationType(authorizationService)
        .operationType(operationType)
        .dataAccess(dataAccess)
        .tokenVerifier(tokenVerifier)
        .build();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private ConfigurationDataAccess dataAccess;
    private TokenVerifier tokenVerifier;

    public Builder dataAccess(ConfigurationDataAccess dataAccess) {
      this.dataAccess = dataAccess;
      return this;
    }

    public Builder tokenVerifier(TokenVerifier tokenVerifier) {
      this.tokenVerifier = tokenVerifier;
      return this;
    }

    /**
     * Constructs a ConfigurationService object.
     *
     * @return a instance of ConfigurationService
     */
    public ConfigurationService build() {
      Objects.requireNonNull(dataAccess, "Data access cannot be null");
      Objects.requireNonNull(tokenVerifier, "Token verifier cannot be null");
      return new ConfigurationServiceImpl(dataAccess, tokenVerifier);
    }
  }
}
