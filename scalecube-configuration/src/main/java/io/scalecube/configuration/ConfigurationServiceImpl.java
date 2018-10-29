package io.scalecube.configuration;

import io.scalecube.configuration.api.AccessRequest;
import io.scalecube.configuration.api.Acknowledgment;
import io.scalecube.configuration.api.BadRequest;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.CreateRepositoryRequest;
import io.scalecube.configuration.api.DeleteRequest;
import io.scalecube.configuration.api.Entries;
import io.scalecube.configuration.api.FetchRequest;
import io.scalecube.configuration.api.FetchResponse;
import io.scalecube.configuration.api.InvalidAuthenticationToken;
import io.scalecube.configuration.api.InvalidPermissionsException;
import io.scalecube.configuration.api.SaveRequest;

import io.scalecube.configuration.repository.ConfigurationDataAccess;
import io.scalecube.configuration.repository.Document;
import io.scalecube.configuration.repository.Repository;
import io.scalecube.configuration.repository.RepositoryEntryKey;
import io.scalecube.configuration.tokens.InvalidAuthenticationException;
import io.scalecube.configuration.tokens.TokenVerifier;

import io.scalecube.security.Profile;

import java.util.Objects;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class ConfigurationServiceImpl implements ConfigurationService {

  private static final Logger logger = LoggerFactory.getLogger(ConfigurationServiceImpl.class);

  private final ConfigurationDataAccess dataAccess;
  private TokenVerifier tokenVerifier;

  private ConfigurationServiceImpl(
      ConfigurationDataAccess dataAccess,
      TokenVerifier tokenVerifier) {
    this.dataAccess = dataAccess;
    this.tokenVerifier = tokenVerifier;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public Mono<Acknowledgment> createRepository(CreateRepositoryRequest request) {

    return Mono.create(result -> {
      try {
        logger.debug("createRepository: enter: request: {}", request);

        validateRequest(request);

        Profile profile = verifyToken(request.token());
        validateProfile(profile);

        Role role = getRole(profile);

        if (role == Role.Owner) {
          Repository repository = repository(profile, request);
          dataAccess.createRepository(repository);
          logger.debug("createRepository: exit: request: {}", request);
          result.success(new Acknowledgment());
        } else {
          Throwable invalidPermissionsException = new InvalidPermissionsException(
              String.format(
                  "Role '%s' has insufficient permissions for the requested operation", role)
          );
          logger.error("createRepository: request: {}, error: {}", request,
              invalidPermissionsException);
          result.error(
              invalidPermissionsException
          );
        }
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
        validateRequest(request);

        Profile profile = verifyToken(request.token());
        validateProfile(profile);

        getRole(profile);

        Document entry = dataAccess.get(key(profile, request, request.key()));
        FetchResponse response = new FetchResponse(request.key(), entry.value());
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
        validateEntriesRequest(request);

        Profile profile = verifyToken(request.token());
        validateProfile(profile);
        logger.debug("entries: profile tenant: {}", profile.getTenant());

        getRole(profile);

        Repository repository = repository(
            profile,
            request);
        FetchResponse[] fetchResponses = dataAccess.entries(repository)
            .stream()
            .map(doc -> FetchResponse.builder()
                .key(doc.key())
                .value(doc.value())
                .build()).toArray(FetchResponse[]::new);
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

        validateRequest(request);
        Profile profile = verifyToken(request.token());
        validateProfile(profile);
        logger.debug("save: profile tenant: {}", profile.getTenant());

        Role role = getRole(profile);

        if (role == Role.Admin || role == Role.Owner) {
          Document document = Document.builder()
              .id(UUID.randomUUID().toString())
              .key(request.key())
              .value(request.value())
              .build();
          RepositoryEntryKey key = key(profile, request, request.key());
          dataAccess.put(key, document);
          logger.debug("save: exit: request: {}", request);
          result.success(new Acknowledgment());
        } else {
          InvalidPermissionsException invalidPermissionsException = new InvalidPermissionsException(
              "invalid permissions-level save request requires write access");
          logger.error("save: request: {}, error: {}", request, invalidPermissionsException);
          result.error(invalidPermissionsException);
        }
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
        validateRequest(request);
        Profile profile = verifyToken(request.token());
        validateProfile(profile);
        logger.debug("delete: profile tenant: {}", profile.getTenant());

        Role role = getRole(profile);

        if (role != Role.Member) {
          RepositoryEntryKey key = key(profile, request, request.key());
          dataAccess.remove(key);
          logger.debug("delete: exit: request: {}", request);
          result.success(new Acknowledgment());
        } else {
          InvalidPermissionsException invalidPermissionsException = new InvalidPermissionsException(
              "invalid permissions-level save request requires write access");
          logger.debug("delete: request: {}, error: {}", request, invalidPermissionsException);
          result.error(invalidPermissionsException);
        }
      } catch (Throwable ex) {
        logger.debug("delete: request: {}, error: {}", request, ex);
        result.error(ex);
      }
    });
  }

  private static RepositoryEntryKey key(Profile profile, AccessRequest request, String key) {
    Repository repository = repository(profile, request);
    return RepositoryEntryKey.builder()
        .repository(repository)
        .key(key)
        .build();
  }

  private static Repository repository(Profile profile, AccessRequest request) {
    return Repository.builder()
        .namespace(profile.getTenant())
        .name(request.repository())
        .build();
  }


  private void validateRequest(CreateRepositoryRequest request) throws BadRequest {
    if (request == null) {
      throw new BadRequest("Request is a required argument");
    }

    if (request.token() == null || request.token().toString().length() == 0) {
      throw new BadRequest("Token is a required argument");
    }

    if (request.repository() == null || request.repository().length() == 0) {
      throw new BadRequest("Repository name is a required argument");
    }
  }

  private void validateRequest(FetchRequest request) throws BadRequest {
    if (request == null) {
      throw new BadRequest("Request is a required argument");
    }

    if (request.token() == null || request.token().toString().length() == 0) {
      throw new BadRequest("Token is a required argument");
    }

    if (request.repository() == null || request.repository().length() == 0) {
      throw new BadRequest("Repository name is a required argument");
    }

    if (request.key() == null || request.key().length() == 0) {
      throw new BadRequest("Key name is a required argument");
    }
  }

  private void validateRequest(SaveRequest request) throws BadRequest {
    if (request == null) {
      throw new BadRequest("Request is a required argument");
    }

    if (request.token() == null || request.token().toString().length() == 0) {
      throw new BadRequest("Token is a required argument");
    }

    if (request.repository() == null || request.repository().length() == 0) {
      throw new BadRequest("Repository name is a required argument");
    }

    if (request.key() == null) {
      throw new BadRequest("Key is a required argument");
    }

    if (request.value() == null) {
      throw new BadRequest("Value is a required argument");
    }
  }

  private void validateRequest(DeleteRequest request) throws BadRequest {
    if (request == null) {
      throw new BadRequest("Request is a required argument");
    }

    if (request.token() == null || request.token().toString().length() == 0) {
      throw new BadRequest("Token is a required argument");
    }

    if (request.repository() == null || request.repository().length() == 0) {
      throw new BadRequest("Repository name is a required argument");
    }

    if (request.key() == null || request.key().length() == 0) {
      throw new BadRequest("Key is a required argument");
    }
  }

  private void  validateProfile(Profile profile) throws InvalidAuthenticationToken {
    if (profile == null) {
      throw new InvalidAuthenticationToken();
    }

    boolean inValidTenant = profile.getTenant() == null || profile.getTenant().length() == 0;

    if (inValidTenant) {
      throw new InvalidAuthenticationToken("missing tenant");
    }

    if (profile.getClaims() == null) {
      throw new InvalidAuthenticationToken("missing claims");
    }
  }

  private void validateEntriesRequest(FetchRequest request) throws BadRequest {
    if (request == null) {
      throw new BadRequest("Request is a required argument");
    }

    if (request.token() == null || request.token().toString().length() == 0) {
      throw new BadRequest("Token is a required argument");
    }

    if (request.repository() == null || request.repository().length() == 0) {
      throw new BadRequest("Repository name is a required argument");
    }
  }

  private Role getRole(Profile profile) throws InvalidAuthenticationToken {
    Objects.requireNonNull(profile, "profile");
    Objects.requireNonNull(profile.getClaims(), "profile.claims");
    Object role = profile.getClaims().get("role");
    boolean invalidRole = role == null || role.toString().length() == 0;

    if (invalidRole) {
      throw new InvalidAuthenticationToken("Invalid role: " + role);
    }
    return Enum.valueOf(Role.class, role.toString());
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
     * @return a instance of ConfigurationService
     */
    public ConfigurationService build() {
      Objects.requireNonNull(dataAccess, "Data access cannot be null");
      Objects.requireNonNull(tokenVerifier, "Token verifier cannot be null");
      return new ConfigurationServiceImpl(dataAccess, tokenVerifier);
    }
  }

  private Profile verifyToken(Object token) throws InvalidAuthenticationException {
    Profile profile = tokenVerifier.verify(token);
    if (profile == null) {
      throw new InvalidAuthenticationException();
    }
    return profile;
  }
}
