package io.scalecube.configuration;

import static io.scalecube.configuration.RequestValidator.validate;

import io.scalecube.configuration.api.Acknowledgment;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.CreateOrUpdateEntryRequest;
import io.scalecube.configuration.api.CreateRepositoryRequest;
import io.scalecube.configuration.api.DeleteEntryRequest;
import io.scalecube.configuration.api.InvalidAuthenticationToken;
import io.scalecube.configuration.api.ReadEntryHistoryRequest;
import io.scalecube.configuration.api.ReadEntryHistoryResponse;
import io.scalecube.configuration.api.ReadEntryRequest;
import io.scalecube.configuration.api.ReadEntryResponse;
import io.scalecube.configuration.api.ReadListRequest;
import io.scalecube.configuration.api.VersionAcknowledgment;
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
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class ConfigurationServiceImpl implements ConfigurationService {

  private static final Logger logger = LoggerFactory.getLogger(ConfigurationServiceImpl.class);
  private static final Acknowledgment ACK = new Acknowledgment();
  private static final VersionAcknowledgment FIRST_VERSION_ACK = new VersionAcknowledgment();

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
        .then(checkAccess(request.apiKey(), ConfigurationService.CONFIG_CREATE_REPO))
        .flatMap(p -> repository.createRepository(new Repository(p.tenant(), request.repository())))
        .map(b -> ACK)
        .doOnSuccess(result -> logger.debug("createRepository: request: {}", request))
        .doOnError(th -> logger.error("createRepository: request: {}, error:", request, th));
  }

  @Override
  public Mono<ReadEntryResponse> readEntry(ReadEntryRequest request) {
    return Mono.fromRunnable(() -> logger.debug("read: enter: request: {}", request))
        .then(Mono.defer(() -> validate(request)))
        .subscribeOn(scheduler)
        .then(checkAccess(request.apiKey(), ConfigurationService.CONFIG_READ_ENTRY))
        .flatMap(
            p ->
                repository.read(
                    p.tenant(), request.repository(), request.key(), intVersion(request.version())))
        .map(document -> new ReadEntryResponse(document.key(), document.value()))
        .doOnSuccess(result -> logger.debug("read: request: {}, result: {}", request, result))
        .doOnError(th -> logger.error("read: request: {}, error:", request, th));
  }

  @Override
  public Mono<List<ReadEntryResponse>> readList(ReadListRequest request) {
    return Mono.fromRunnable(() -> logger.debug("readAll: enter: request: {}", request))
        .then(Mono.defer(() -> validate(request)))
        .subscribeOn(scheduler)
        .then(checkAccess(request.apiKey(), ConfigurationService.CONFIG_READ_LIST))
        .flatMapMany(
            p ->
                repository.readAll(p.tenant(), request.repository(), intVersion(request.version())))
        .map(doc -> new ReadEntryResponse(doc.key(), doc.value()))
        .collectList()
        .doOnSuccess(result -> logger.debug("readAll: request: {}, result: {}", request, result))
        .doOnError(th -> logger.error("readAll: request: {}, error:", request, th));
  }

  @Override
  public Mono<List<ReadEntryHistoryResponse>> readEntryHistory(ReadEntryHistoryRequest request) {
    return Mono.fromRunnable(() -> logger.debug("readHistory: enter: request: {}", request))
        .then(Mono.defer(() -> validate(request)))
        .subscribeOn(scheduler)
        .then(checkAccess(request.apiKey(), ConfigurationService.CONFIG_READ_ENTRY_HISTORY))
        .flatMapMany(p -> repository.readHistory(p.tenant(), request.repository(), request.key()))
        .map(doc -> new ReadEntryHistoryResponse(doc.version(), doc.value()))
        .collectList()
        .doOnSuccess(
            result -> logger.debug("readHistory: exit: request: {}, result: {}", request, result))
        .doOnError(th -> logger.error("readHistory: request: {}, error:", request, th));
  }

  @Override
  public Mono<VersionAcknowledgment> createEntry(CreateOrUpdateEntryRequest request) {
    return Mono.fromRunnable(() -> logger.debug("create: enter: request: {}", request))
        .then(Mono.defer(() -> validate(request)))
        .subscribeOn(scheduler)
        .then(checkAccess(request.apiKey(), ConfigurationService.CONFIG_CREATE_ENTRY))
        .flatMap(
            p ->
                repository.save(
                    p.tenant(), request.repository(), new Document(request.key(), request.value())))
        .thenReturn(FIRST_VERSION_ACK)
        .doOnSuccess(result -> logger.debug("create: request: {}", request))
        .doOnError(th -> logger.error("create: request: {}, error:", request, th));
  }

  @Override
  public Mono<VersionAcknowledgment> updateEntry(CreateOrUpdateEntryRequest request) {
    return Mono.fromRunnable(() -> logger.debug("update: enter: request: {}", request))
        .then(Mono.defer(() -> validate(request)))
        .subscribeOn(scheduler)
        .then(checkAccess(request.apiKey(), ConfigurationService.CONFIG_CREATE_ENTRY))
        .flatMap(
            p ->
                repository.update(
                    p.tenant(), request.repository(), new Document(request.key(), request.value())))
        .map(document -> new VersionAcknowledgment(document.version()))
        .doOnSuccess(result -> logger.debug("update: exit: request: {}", request))
        .doOnError(th -> logger.error("update: request: {}, error:", request, th));
  }

  @Override
  public Mono<Acknowledgment> deleteEntry(DeleteEntryRequest request) {
    return Mono.fromRunnable(() -> logger.debug("delete: enter: request: {}", request))
        .then(Mono.defer(() -> validate(request)))
        .subscribeOn(scheduler)
        .then(checkAccess(request.apiKey(), ConfigurationService.CONFIG_DELETE_ENTRY))
        .flatMap(p -> repository.delete(p.tenant(), request.repository(), request.key()))
        .thenReturn(ACK)
        .doOnSuccess(result -> logger.debug("delete: request: {}", request))
        .doOnError(th -> logger.error("delete: request: {}, error:", request, th));
  }

  private static Integer intVersion(Object version) {
    if (version instanceof Integer) {
      return (Integer) version;
    } else if (version instanceof Long) {
      return Integer.valueOf(version.toString());
    } else if (version instanceof String) {
      return Integer.valueOf((String) version);
    }

    return (Integer) version;
  }

  private Mono<Profile> checkAccess(Object apiKey, String resource) {
    return Mono.defer(
        () ->
            accessControl
                .check(apiKey.toString(), resource)
                .onErrorMap(AuthenticationException.class, e -> new InvalidAuthenticationToken()));
  }
}
