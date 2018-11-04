package io.scalecube.configuration.authorization;

import io.scalecube.configuration.Role;
import io.scalecube.configuration.api.InvalidPermissionsException;

import java.util.Objects;

/**
 * Encapsulates and enforces RBAC policy.
 */
public final class AuthorizationService {

  private AuthorizationService() {
  }

  /**
   * Determines if the role argument is autorize to perform the operationType argument.
   * @param role the role criteria
   * @param operationType the operation type criteria
   * @throws InvalidPermissionsException in case the role has insufficient permissions for the
   *     requested operation
   */
  public void authorize(Role role, OperationType operationType) throws InvalidPermissionsException {
    Objects.requireNonNull(role, "role");
    boolean unauthorized = false;

    switch (operationType) {
      case Write:
      case Delete:
        unauthorized = role == Role.Member;
        break;
      case CreateRepoitory:
        unauthorized = role != Role.Owner;
        break;
      case Read:
      case List:
        break;
      default:
        throw new IllegalStateException();
    }

    if (unauthorized) {
      throw new InvalidPermissionsException(
          String.format(
              "Role '%s' has insufficient permissions for the requested operation: %s",
              role, operationType)
      );
    }
  }

  /**
   * Returns a Builder instance of this class.
   * @return a Builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    public AuthorizationService build() {
      return new AuthorizationService();
    }
  }
}
