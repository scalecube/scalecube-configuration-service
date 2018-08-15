package io.scalecube.configuration;

import io.scalecube.configuration.api.Acknowledgment;
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
import io.scalecube.configuration.tokens.TokenVerifier;
import io.scalecube.security.Profile;

import reactor.core.publisher.Mono;

import java.util.Objects;

public class ConfigurationServiceImpl implements ConfigurationService {

  private final ConfigurationDataAccess<Document> dataAccess;
  private TokenVerifier tokenVerifier;

  private ConfigurationServiceImpl(ConfigurationDataAccess<Document> dataAccess) {
    this.dataAccess = dataAccess;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public Mono<Acknowledgment> createRepository(CreateRepositoryRequest request) {
    Objects.requireNonNull(request, "request is a required argument");

    return Mono.create(result -> {
      try {
        Profile profile = tokenVerifier.verify(request.token());
        if (profile != null) {
          if (!profile.getClaims().containsKey("org")) {
            result.error(new InvalidAuthenticationToken("missing org claim"));
          }
          dataAccess.createRepository(
              profile.getClaims().get("org").toString(),
              request.repository());
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

    private ConfigurationDataAccess<Document> dataAccess;

    public Builder dataAccess(ConfigurationDataAccess<Document> dataAccess) {
      this.dataAccess = dataAccess;
      return this;
    }

    public ConfigurationService build() {
      Objects.requireNonNull(dataAccess, "Data access cannot be null");
      return new ConfigurationServiceImpl(dataAccess);
    }
  }
}
