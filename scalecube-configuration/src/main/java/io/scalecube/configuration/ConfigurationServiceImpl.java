package io.scalecube.configuration;

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
import io.scalecube.configuration.tokens.InvalidAuthenticationException;
import io.scalecube.configuration.tokens.TokenVerifier;
import io.scalecube.security.Profile;

import reactor.core.publisher.Mono;

import java.util.Objects;

public class ConfigurationServiceImpl implements ConfigurationService {

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
        validateRequest(request);
        Profile profile = tokenVerifier.verify(request.token());
        validateProfile(profile);
        Role role = getRole(profile);

        if (role == Role.Owner) {
          dataAccess.createRepository(profile.getTenant(), request.repository());
          result.success(new Acknowledgment());
        } else {
          result.error(
              new InvalidPermissionsException(
                String.format(
                  "Role '%s' has insufficient permissions for the requested operation", role)
                )
              );
        }
      } catch (Throwable ex) {
        result.error(ex);
      }
    });
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

  private void validateProfile(Profile profile) throws InvalidAuthenticationToken {

    if(profile == null) {
      throw new InvalidAuthenticationToken();
    }

    boolean inValidTenant = profile.getTenant() == null || profile.getTenant().length() == 0;
    if (inValidTenant) {
      throw new InvalidAuthenticationToken("missing tenant");
    }

    if(profile.getClaims() == null) {
      throw new InvalidAuthenticationToken("missing claims");
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

  @Override
  public Mono<FetchResponse> fetch(FetchRequest request) {
    Objects.requireNonNull(request, "request is a required argument");

    return Mono.create(result -> {
      try {
        Profile profile = tokenVerifier.verify(request.token());
        if (profile != null) {
          if (!profile.getClaims().containsKey("org")) {
            result.error(new InvalidAuthenticationToken("missing org claim"));
          }
          Document entry = dataAccess.get(
              profile.getClaims().get("org").toString(),
              request.repository(),
              request.key());
          result.success(new FetchResponse(request.key(), entry.value()));
        } else {
          result.error(new InvalidAuthenticationToken());
        }
      } catch (Exception ex) {
        result.error(ex);
      }
    });
  }

  @Override
  public Mono<Entries<FetchResponse>> entries(FetchRequest request) {
    return Mono.create(result -> {
      try {
        Profile profile = tokenVerifier.verify(request.token());
        if (profile != null) {
          if (!profile.getClaims().containsKey("org")) {
            result.error(new InvalidAuthenticationToken("missing org claim"));
          }
          FetchResponse[] fetchResponses = dataAccess.entries(
              profile.getClaims().get("org").toString(),
              request.repository())
              .stream()
              .map(doc -> FetchResponse.builder()
                  .key(doc.key())
                  .value(doc.value())
                  .build()).toArray(FetchResponse[]::new);
          result.success(new Entries<>(fetchResponses));
        } else {
          result.error(new InvalidAuthenticationToken());
        }
      } catch (Exception ex) {
        result.error(ex);
      }
    });
  }

  @Override
  public Mono<Acknowledgment> save(SaveRequest request) {
    Objects.requireNonNull(request, "request is a required argument");

    return Mono.create(result -> {
      try {
        Profile profile = tokenVerifier.verify(request.token());
        if (profile != null) {
          if (!profile.getClaims().containsKey("org")) {
            result.error(new InvalidAuthenticationToken("missing org claim"));
            return;
          }

          if (!profile.getClaims().containsKey("role")) {
            result.error(new InvalidAuthenticationToken("missing role claim"));
            return;
          }

          Role role = getRole(profile.getClaims().get("role").toString());

          if (role == Role.Member) {
            result.error(new InvalidPermissionsException(
                "invalid permissions-level save request requires write access"));
            return;
          }

          Document doc = Document.builder()
              .key(request.key())
              .value(request.value())
              .build();
          dataAccess.put(profile.getClaims().get("org").toString(),
              request.repository(),
              request.key(),
              doc
          );
          result.success(new Acknowledgment());
        } else {
          result.error(new InvalidAuthenticationToken());
        }
      } catch (Exception ex) {
        result.error(ex);
      }
    });
  }

  @Override
  public Mono<Acknowledgment> delete(DeleteRequest request) {
    Objects.requireNonNull(request, "request is a required argument");

    return Mono.create(result -> {
      try {
        Profile profile = tokenVerifier.verify(request.token());
        if (profile != null) {
          if (!profile.getClaims().containsKey("org")) {
            result.error(new InvalidAuthenticationToken("missing org claim"));
            return;
          }

          if (!profile.getClaims().containsKey("role")) {
            result.error(new InvalidAuthenticationToken("missing role claim"));
            return;
          }

          Role role = getRole(profile.getClaims().get("role").toString());

          if (role == Role.Member) {
            result.error(new InvalidPermissionsException(
                "invalid permissions-level save request requires write access"));
            return;
          }

          dataAccess.remove(profile.getClaims().get("org").toString(),
              request.repository(),
              request.key()
          );
          result.success(new Acknowledgment());
        } else {
          result.error(new InvalidAuthenticationToken());
        }
      } catch (Exception ex) {
        result.error(ex);
      }
    });
  }

  private Role getRole(String role) {
    return Enum.valueOf(Role.class, role);
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


    public ConfigurationService build() {
      Objects.requireNonNull(dataAccess, "Data access cannot be null");
      Objects.requireNonNull(tokenVerifier, "Data access cannot be null");
      return new ConfigurationServiceImpl(dataAccess, tokenVerifier);
    }
  }
}
