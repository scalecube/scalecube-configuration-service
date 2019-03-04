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
import io.scalecube.configuration.repository.ConfigurationRepository;
import io.scalecube.configuration.repository.Document;
import io.scalecube.configuration.repository.Repository;
import io.scalecube.security.api.AccessControl;
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
    return validateRequest(request)
        .subscribeOn(scheduler)
        .then(
            Mono.defer(
                () ->
                    accessControl
                        .check(request.token().toString(), ConfigurationService.CONFIG_CREATE_REPO)
                        .flatMap(
                            p ->
                                repository.createRepository(
                                    new Repository(p.tenant(), request.repository())))
                        .map(b -> ACK)))
        .doOnSuccess(result -> logger.debug("createRepository: exit: request: {}", request))
        .doOnError(th -> logger.error("createRepository: request: {}, error:", request, th));
  }

  @Override
  public Mono<FetchResponse> fetch(FetchRequest request) {
    return validateRequest(request)
        .subscribeOn(scheduler)
        .then(
            Mono.defer(
                () ->
                    accessControl
                        .check(request.token().toString(), ConfigurationService.CONFIG_FETCH)
                        .flatMap(
                            p -> repository.fetch(p.tenant(), request.repository(), request.key()))
                        .map(Document::value)
                        .map(value -> new FetchResponse(request.key(), value))))
        .doOnSuccess(result -> logger.debug("fetch: exit: request: {}", request))
        .doOnError(th -> logger.error("fetch: request: {}, error:", request, th));
  }

  @Override
  public Flux<FetchResponse> entries(FetchRequest request) {
    return validateRequest(request)
        .subscribeOn(scheduler)
        .thenMany(
            Flux.defer(
                () ->
                    accessControl
                        .check(request.token().toString(), ConfigurationService.CONFIG_ENTRIES)
                        .flatMapMany(p -> repository.fetchAll(p.tenant(), request.repository()))
                        .map(doc -> new FetchResponse(doc.key(), doc.value()))))
        .doOnComplete(() -> logger.debug("entries: exit: request: {}", request))
        .doOnError(th -> logger.error("entries: request: {}, error:", request, th));
  }

  @Override
  public Mono<List<FetchResponse>> collectEntries(FetchRequest request) {
    return entries(request).collectList();
  }

  @Override
  public Mono<Acknowledgment> save(SaveRequest request) {
    return validateRequest(request)
        .subscribeOn(scheduler)
        .then(
            Mono.defer(
                () ->
                    accessControl
                        .check(request.token().toString(), ConfigurationService.CONFIG_SAVE)
                        .flatMap(
                            p ->
                                repository.save(
                                    p.tenant(),
                                    request.repository(),
                                    Document.builder()
                                        .id(IdGenerator.generateId())
                                        .key(request.key())
                                        .value(request.value())
                                        .build()))
                        .thenReturn(ACK)))
        .doOnSuccess(result -> logger.debug("save: exit: request: {}", request))
        .doOnError(th -> logger.error("save: request: {}, error:", request, th));
  }

  @Override
  public Mono<Acknowledgment> delete(DeleteRequest request) {
    return validateRequest(request)
        .subscribeOn(scheduler)
        .then(
            Mono.defer(
                () ->
                    accessControl
                        .check(request.token().toString(), ConfigurationService.CONFIG_DELETE)
                        .flatMap(
                            p -> repository.delete(p.tenant(), request.repository(), request.key()))
                        .thenReturn(ACK)))
        .doOnSuccess(result -> logger.debug("delete: exit: request: {}", request))
        .doOnError(th -> logger.error("delete: request: {}, error:", request, th));
  }

  private static Mono<Void> validateRequest(AccessRequest request) {
    return Mono.fromRunnable(
        () -> {
          requireNonNull(request, "request null is invalid");
          requireNonNull(request.token(), "request.token null is invalid");
          requireNonNull(request.repository(), "request.repository null is invalid");
        });
  }
}
