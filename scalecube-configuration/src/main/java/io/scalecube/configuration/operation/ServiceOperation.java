package io.scalecube.configuration.operation;

import io.scalecube.configuration.Role;
import io.scalecube.configuration.api.AccessRequest;
import io.scalecube.configuration.api.BadRequest;
import io.scalecube.configuration.api.InvalidAuthenticationToken;
import io.scalecube.configuration.repository.Repository;
import io.scalecube.configuration.repository.RepositoryEntryKey;
import io.scalecube.configuration.tokens.TokenVerifier;
import io.scalecube.security.api.Profile;
import java.util.Objects;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

/**
 * Abstraction of a Service Operation, encapsulates the logic of the operation and creates a blue
 * print for concrete service operations. Enables SRP of each service operations.
 *
 * @param <I> Service call request
 * @param <O> Service call response
 */
public abstract class ServiceOperation<I extends AccessRequest, O> {

  private static final String ROLE_CLAIM_NAME = "role";

  /**
   * Executes the request argument.
   *
   * @param request the service request
   * @param context execution context
   * @return service call response
   */
  public Publisher<O> execute(I request, ServiceOperationContext context) {
    return Mono.fromRunnable(() -> validate(request))
        .then(Mono.defer(() -> verifyToken(context.tokenVerifier(), request.token())))
        .switchIfEmpty(
            Mono.defer(() -> Mono.error(new InvalidAuthenticationToken("profile is null"))))
        .doOnNext(
            profile -> {
              validateProfile(profile);
              authorize(getRole(profile), context);
            })
        .flatMapMany(profile -> process(request, profile, context));
  }

  protected void validate(I request) {
    if (request == null) {
      throw new BadRequest("Request is a required argument");
    }

    if (request.token() == null || request.token().toString().length() == 0) {
      throw new BadRequest("Token is a required argument");
    }
  }

  private Mono<Profile> verifyToken(TokenVerifier tokenVerifier, Object token) {
    return tokenVerifier.verify(token);
  }

  private void validateProfile(Profile profile) {
    if (profile == null) {
      throw new InvalidAuthenticationToken("profile is null");
    }

    boolean inValidTenant = profile.tenant() == null || profile.tenant().length() == 0;

    if (inValidTenant) {
      throw new InvalidAuthenticationToken("missing tenant");
    }

    if (profile.claims() == null) {
      throw new InvalidAuthenticationToken("missing claims");
    }
  }

  private void authorize(Role role, ServiceOperationContext context) {
    context.authorizationService().authorize(role, context.operationType());
  }

  private Role getRole(Profile profile) {
    Objects.requireNonNull(profile, "profile");
    Objects.requireNonNull(profile.claims(), "profile.claims");
    Object role = profile.claims().get(ROLE_CLAIM_NAME);
    boolean invalidRole = role == null || role.toString().length() == 0;

    if (invalidRole) {
      throw new InvalidAuthenticationToken("Invalid role: " + role);
    }
    return Enum.valueOf(Role.class, role.toString());
  }

  protected abstract Publisher<O> process(
      I request, Profile profile, ServiceOperationContext context);

  protected static Repository repository(Profile profile, AccessRequest request) {
    return Repository.builder().namespace(profile.tenant()).name(request.repository()).build();
  }

  protected static RepositoryEntryKey key(Profile profile, AccessRequest request, String key) {
    Repository repository = repository(profile, request);
    return RepositoryEntryKey.builder().repository(repository).key(key).build();
  }
}
