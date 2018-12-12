package io.scalecube.configuration.operation;

import io.scalecube.configuration.Role;
import io.scalecube.configuration.api.AccessRequest;
import io.scalecube.configuration.api.BadRequest;
import io.scalecube.configuration.api.InvalidAuthenticationToken;
import io.scalecube.configuration.api.InvalidPermissionsException;

import io.scalecube.configuration.repository.Repository;

import io.scalecube.configuration.repository.RepositoryEntryKey;
import io.scalecube.configuration.tokens.InvalidAuthenticationException;
import io.scalecube.configuration.tokens.TokenVerifier;

import io.scalecube.security.Profile;

import java.util.Objects;

/**
 * Abstraction of a Service Operation, encapsulates the logic of the operation and creates a
 * blue print for concrete service operations.
 * Enables SRP of each service operations.
 *
 * @param <I> Service call request
 * @param <O> Service call response
 */
public abstract class ServiceOperation<I extends AccessRequest, O> {

  public static final String ROLE_CLAIM_NAME = "role";

  /**
   * Executes the request argument.
   *
   * @param request the service request
   * @param context execution context
   * @return service call response
   * @throws Throwable in case of an error
   */
  public O execute(I request, ServiceOperationContext context) throws Throwable {
    validate(request);
    Profile profile = verifyToken(context.tokenVerifier(), request.token());
    validateProfile(profile);
    authorize(getRole(profile), context);
    return process(request, profile, context);
  }

  protected void validate(I request) throws Throwable {
    if (request == null) {
      throw new BadRequest("Request is a required argument");
    }

    if (request.token() == null || request.token().toString().length() == 0) {
      throw new BadRequest("Token is a required argument");
    }

  }

  private Profile verifyToken(TokenVerifier tokenVerifier, Object token) throws
      InvalidAuthenticationException {
    Profile profile = tokenVerifier.verify(token);
    if (profile == null) {
      throw new InvalidAuthenticationException();
    }
    return profile;
  }

  private void validateProfile(Profile profile) throws InvalidAuthenticationToken {
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

  private void authorize(Role role, ServiceOperationContext context) throws
      InvalidPermissionsException {
    context.authorizationService().authorize(role, context.operationType());
  }

  private Role getRole(Profile profile) throws InvalidAuthenticationToken {
    Objects.requireNonNull(profile, "profile");
    Objects.requireNonNull(profile.getClaims(), "profile.claims");
    Object role = profile.getClaims().get(ROLE_CLAIM_NAME);
    boolean invalidRole = role == null || role.toString().length() == 0;

    if (invalidRole) {
      throw new InvalidAuthenticationToken("Invalid role: " + role);
    }
    return Enum.valueOf(Role.class, role.toString());
  }

  protected abstract O process(I request,
                               Profile profile,
                               ServiceOperationContext context);


  protected static Repository repository(Profile profile, AccessRequest request) {
    return Repository.builder()
        .namespace(profile.getTenant())
        .name(request.repository())
        .build();
  }

  protected static RepositoryEntryKey key(Profile profile, AccessRequest request, String key) {
    Repository repository = repository(profile, request);
    return RepositoryEntryKey.builder()
        .repository(repository)
        .key(key)
        .build();
  }
}
