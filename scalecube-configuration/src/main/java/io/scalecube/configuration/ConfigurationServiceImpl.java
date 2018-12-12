package io.scalecube.configuration;

import io.scalecube.configuration.api.AccessRequest;
import io.scalecube.configuration.api.Acknowledgment;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.CreateRepositoryRequest;
import io.scalecube.configuration.api.DeleteRequest;
import io.scalecube.configuration.api.Entries;
import io.scalecube.configuration.api.FetchRequest;
import io.scalecube.configuration.api.FetchResponse;
import io.scalecube.configuration.api.SaveRequest;

import io.scalecube.configuration.authorization.AuthorizationService;
import io.scalecube.configuration.authorization.OperationType;
import io.scalecube.configuration.operation.ServiceOperationContext;
import io.scalecube.configuration.operation.ServiceOperationFactory;
import io.scalecube.configuration.repository.ConfigurationDataAccess;
import io.scalecube.configuration.repository.Repository;
import io.scalecube.configuration.tokens.TokenVerifier;

import io.scalecube.security.Profile;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class ConfigurationServiceImpl implements ConfigurationService {

  private static final Logger logger = LoggerFactory.getLogger(ConfigurationServiceImpl.class);

  private final ConfigurationDataAccess dataAccess;
  private TokenVerifier tokenVerifier;
  private final AuthorizationService authorizationService;

  private ConfigurationServiceImpl(
      ConfigurationDataAccess dataAccess,
      TokenVerifier tokenVerifier) {
    this.dataAccess = dataAccess;
    this.tokenVerifier = tokenVerifier;
    this.authorizationService = AuthorizationService.builder().build();
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public Mono<Acknowledgment> createRepository(CreateRepositoryRequest request) {

    return Mono.create(result -> {
      try {
        logger.debug("createRepository: enter: request: {}", request);
        Acknowledgment acknowledgment = ServiceOperationFactory.createRepository().execute(
            request, context(OperationType.CreateRepoitory));
        logger.debug("createRepository: exit: request: {}", request);
        result.success(acknowledgment);
      } catch (Throwable ex) {
        logger.error("createRepository: request: {}, error: {}", request, ex);
        result.error(ex);
      }
    });
  }


  @Override
  public Mono<FetchResponse> fetch(FetchRequest request) {
    return Mono.create(result -> {
      try {
        logger.debug("fetch: enter: request: {}", request);
        FetchResponse response = ServiceOperationFactory.fetch().execute(request,
            context(OperationType.Read));
        logger.debug("fetch: exit: request: {}, response: {}", request, response);
        result.success(response);
      } catch (Throwable ex) {
        logger.debug("fetch: exit: request: {}, error: {}", request, ex);
        result.error(ex);
      }
    });
  }


  @Override
  public Mono<Entries<FetchResponse>> entries(FetchRequest request) {
    return Mono.create(result -> {
      try {
        logger.debug("entries: enter: request: {}", request);
        FetchResponse[] fetchResponses = ServiceOperationFactory.fetchAll().execute(
            request, context(OperationType.List));

        logger.debug("entries: exit: request: {}, return {} entries", request,
            fetchResponses.length);
        result.success(new Entries<>(fetchResponses));
      } catch (Throwable ex) {
        logger.debug("entries: request: {}, error: {}", request, ex);
        result.error(ex);
      }
    });
  }


  @Override
  public Mono<Acknowledgment> save(SaveRequest request) {
    return Mono.create(result -> {
      try {
        logger.debug("save: enter: request: {}", request);
        Acknowledgment acknowledgment = ServiceOperationFactory.saveEntry().execute(
            request, context(OperationType.Write));
        logger.debug("save: exit: request: {}", request);
        result.success(acknowledgment);
      } catch (Throwable ex) {
        logger.error("save: request: {}, error: {}", request, ex);
        result.error(ex);
      }
    });
  }


  @Override
  public Mono<Acknowledgment> delete(DeleteRequest request) {
    return Mono.create(result -> {
      try {
        logger.debug("delete: enter: request: {}", request);
        Acknowledgment acknowledgment = ServiceOperationFactory.deleteEntry().execute(
            request, context(OperationType.Delete));
        logger.debug("delete: exit: request: {}", request);
        result.success(acknowledgment);
      } catch (Throwable ex) {
        logger.debug("delete: request: {}, error: {}", request, ex);
        result.error(ex);
      }
    });
  }

  private ServiceOperationContext context(OperationType operationType) {
    return ServiceOperationContext.builder()
        .operationType(authorizationService)
        .operationType(operationType)
        .dataAccess(dataAccess)
        .tokenVerifier(tokenVerifier)
        .build();
  }

  /**
   * Service builder class.
   */
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
