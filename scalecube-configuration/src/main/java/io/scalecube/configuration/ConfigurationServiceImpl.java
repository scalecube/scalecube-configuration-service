package io.scalecube.configuration;

import static io.scalecube.configuration.RequestValidator.validate;

import io.scalecube.configuration.api.Acknowledgment;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.CreateRepositoryRequest;
import io.scalecube.configuration.api.DeleteEntryRequest;
import io.scalecube.configuration.api.ReadEntryRequest;
import io.scalecube.configuration.api.ReadListRequest;
import io.scalecube.configuration.api.ReadEntryResponse;
import io.scalecube.configuration.api.InvalidAuthenticationToken;
import io.scalecube.configuration.api.CreateEntryRequest;
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
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
  public Mono<ReadEntryResponse> readEntry(ReadEntryRequest request) {
    return Mono.fromRunnable(() -> logger.debug("readEntry: enter: request: {}", request))
        .then(Mono.defer(() -> validate(request)))
        .subscribeOn(scheduler)
        .then(
            Mono.defer(
                () -> checkAccess(request.apiKey().toString(),
                    ConfigurationService.CONFIG_READ_ENTRY)))
        .flatMap(p -> repository.readEntry(p.tenant(), request.repository(), request.key()))
        .map(document -> new ReadEntryResponse(document.key(), document.value()))
        .doOnSuccess(
            result -> logger.debug("readEntry: exit: request: {}, result: {}", request, result))
        .doOnError(th -> logger.error("readEntry: request: {}, error:", request, th));
  }

  @Override
  public Mono<List<ReadEntryResponse>> readList(ReadListRequest request) {
    return Mono.fromRunnable(() -> logger.debug("readList: enter: request: {}", request))
        .then(Mono.defer(() -> validate(request)))
        .subscribeOn(scheduler)
        .thenMany(
            Flux.defer(
                () -> checkAccess(request.apiKey().toString(),
                    ConfigurationService.CONFIG_READ_LIST)))
        .flatMap(p -> repository.readList(p.tenant(), request.repository()))
        .map(doc -> new ReadEntryResponse(doc.key(), doc.value()))
        .collectList()
        .doOnSuccess(
            result -> logger.debug("readList: exit: request: {}, result: {}", request, result))
        .doOnError(th -> logger.error("readList: request: {}, error:", request, th));
  }

  @Override
  public Mono<Acknowledgment> createEntry(CreateEntryRequest request) {
    return Mono.fromRunnable(() -> logger.debug("createEntry: enter: request: {}", request))
        .then(Mono.defer(() -> validate(request)))
        .subscribeOn(scheduler)
        .then(
            Mono.defer(
                () -> checkAccess(request.apiKey().toString(),
                    ConfigurationService.CONFIG_CREATE_ENTRY)))
        .flatMap(
            p ->
                repository.createEntry(
                    p.tenant(), request.repository(), new Document(request.key(), request.value())))
        .thenReturn(ACK)
        .doOnSuccess(result -> logger.debug("createEntry: exit: request: {}", request))
        .doOnError(th -> logger.error("createEntry: request: {}, error:", request, th));
  }

  @Override
  public Mono<Acknowledgment> updateEntry(CreateEntryRequest request) {
    throw new NotImplementedException();
  }

  @Override
  public Mono<Acknowledgment> deleteEntry(DeleteEntryRequest request) {
    return Mono.fromRunnable(() -> logger.debug("deleteEntry: enter: request: {}", request))
        .then(Mono.defer(() -> validate(request)))
        .subscribeOn(scheduler)
        .then(
            Mono.defer(
                () -> checkAccess(request.apiKey().toString(),
                    ConfigurationService.CONFIG_DELETE_ENTRY)))
        .flatMap(p -> repository.deleteEntry(p.tenant(), request.repository(), request.key()))
        .thenReturn(ACK)
        .doOnSuccess(result -> logger.debug("deleteEntry: exit: request: {}", request))
        .doOnError(th -> logger.error("deleteEntry: request: {}, error:", request, th));
  }

  private Mono<Profile> checkAccess(String token, String resource) {
    return accessControl
        .check(token, resource)
        .onErrorMap(AuthenticationException.class, e -> new InvalidAuthenticationToken());
  }
}
