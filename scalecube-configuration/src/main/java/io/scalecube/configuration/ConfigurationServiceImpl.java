package io.scalecube.configuration;

import static io.scalecube.configuration.RequestValidator.validate;

import io.scalecube.configuration.api.Acknowledgment;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.CreateRepositoryRequest;
import io.scalecube.configuration.api.DeleteRequest;
import io.scalecube.configuration.api.EntriesRequest;
import io.scalecube.configuration.api.FetchRequest;
import io.scalecube.configuration.api.FetchResponse;
import io.scalecube.configuration.api.InvalidAuthenticationToken;
import io.scalecube.configuration.api.SaveRequest;
import io.scalecube.configuration.repository.ConfigurationRepository;
import io.scalecube.configuration.repository.Document;
import io.scalecube.configuration.repository.Repository;
import io.scalecube.security.api.AccessControl;
import io.scalecube.security.api.Profile;
import io.scalecube.security.jwt.AuthenticationException;
import java.util.List;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class ConfigurationServiceImpl implements ConfigurationService {

  private static final Logger logger = LoggerFactory.getLogger(ConfigurationServiceImpl.class);
  private static final Acknowledgment ACK = new Acknowledgment();

  private final ConfigurationRepository repository;
  private final AccessControl accessControl;
  private final Scheduler scheduler =
      Schedulers.fromExecutor(
          Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));

  public ConfigurationServiceImpl(ConfigurationRepository repository, AccessControl accessControl) {
    this.repository = repository;
    this.accessControl = accessControl;
  }

  @Override
  public Mono<Acknowledgment> createRepository(CreateRepositoryRequest request) {
    return Mono.fromRunnable(() -> logger.debug("createRepository: enter: request: {}", request))
        .then(Mono.defer(() -> validate(request)))
        .subscribeOn(scheduler)
        .then(
            Mono.defer(
                () ->
                    checkAccess(
                        request.apiKey().toString(), ConfigurationService.CONFIG_CREATE_REPO)))
        .flatMap(p -> repository.createRepository(new Repository(p.tenant(), request.repository())))
        .map(b -> ACK)
        .doOnSuccess(result -> logger.debug("createRepository: exit: request: {}", request))
        .doOnError(th -> logger.error("createRepository: request: {}, error:", request, th));
  }

  @Override
  public Mono<FetchResponse> fetch(FetchRequest request) {
    return Mono.fromRunnable(() -> logger.debug("readEntry: enter: request: {}", request))
        .then(Mono.defer(() -> validate(request)))
        .subscribeOn(scheduler)
        .then(
            Mono.defer(
                () -> checkAccess(request.apiKey().toString(), ConfigurationService.CONFIG_FETCH)))
        .flatMap(p -> repository.readEntry(p.tenant(), request.repository(), request.key()))
        .map(document -> new FetchResponse(document.key(), document.value()))
        .doOnSuccess(
            result -> logger.debug("readEntry: exit: request: {}, result: {}", request, result))
        .doOnError(th -> logger.error("readEntry: request: {}, error:", request, th));
  }

  @Override
  public Mono<List<FetchResponse>> entries(EntriesRequest request) {
    return Mono.fromRunnable(() -> logger.debug("entries: enter: request: {}", request))
        .then(Mono.defer(() -> validate(request)))
        .subscribeOn(scheduler)
        .thenMany(
            Flux.defer(
                () -> checkAccess(request.apiKey().toString(),
                    ConfigurationService.CONFIG_ENTRIES)))
        .flatMap(p -> repository.fetchAll(p.tenant(), request.repository()))
        .map(doc -> new FetchResponse(doc.key(), doc.value()))
        .collectList()
        .doOnSuccess(
            result -> logger.debug("entries: exit: request: {}, result: {}", request, result))
        .doOnError(th -> logger.error("entries: request: {}, error:", request, th));
  }

  @Override
  public Mono<Acknowledgment> save(SaveRequest request) {
    return Mono.fromRunnable(() -> logger.debug("createEntry: enter: request: {}", request))
        .then(Mono.defer(() -> validate(request)))
        .subscribeOn(scheduler)
        .then(
            Mono.defer(
                () -> checkAccess(request.apiKey().toString(), ConfigurationService.CONFIG_SAVE)))
        .flatMap(
            p ->
                repository.createEntry(
                    p.tenant(), request.repository(), new Document(request.key(), request.value())))
        .thenReturn(ACK)
        .doOnSuccess(result -> logger.debug("createEntry: exit: request: {}", request))
        .doOnError(th -> logger.error("createEntry: request: {}, error:", request, th));
  }

  @Override
  public Mono<Acknowledgment> delete(DeleteRequest request) {
    return Mono.fromRunnable(() -> logger.debug("delete: enter: request: {}", request))
        .then(Mono.defer(() -> validate(request)))
        .subscribeOn(scheduler)
        .then(
            Mono.defer(
                () -> checkAccess(request.apiKey().toString(), ConfigurationService.CONFIG_DELETE)))
        .flatMap(p -> repository.delete(p.tenant(), request.repository(), request.key()))
        .thenReturn(ACK)
        .doOnSuccess(result -> logger.debug("delete: exit: request: {}", request))
        .doOnError(th -> logger.error("delete: request: {}, error:", request, th));
  }

  private Mono<Profile> checkAccess(String token, String resource) {
    return accessControl
        .check(token, resource)
        .onErrorMap(AuthenticationException.class, e -> new InvalidAuthenticationToken());
  }
}
