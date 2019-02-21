package io.scalecube.configuration.authorization;

import io.scalecube.account.api.Role;
import io.scalecube.security.api.Authorizer;
import io.scalecube.security.api.Profile;
import java.security.AccessControlException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import reactor.core.publisher.Mono;

/**
 * This is a helper class for setting up an immutable Permissions object.
 *
 * <pre>
 * Permissions:
 *   name: resourceName
 *   permissions:
 *     allowed:
 *     - role1
 *     - role2
 * </pre>
 */
public class Permissions implements Authorizer {

  private final Map<String, Set<String>> rolesForAllResources;

  public static class Builder {

    private final Map<String, Set<String>> permissions = new HashMap<>();

    /**
     * grant access to list of roles for a certain action.
     *
     * @param resourceName name or topic of granting access.
     * @param roles or roles allowed to access or do an action names are trimmed of whitespace and
     *     lowercased.
     * @return builder.
     */
    public Permissions.Builder grant(String resourceName, Role... roles) {
      for (Role subject : roles) {
        permissions
            .computeIfAbsent(resourceName, newAction -> new HashSet<>())
            .add(subject.toString());
      }
      return this;
    }

    public Authorizer build() {
      return new Permissions(this);
    }
  }

  private Permissions(Builder builder) {
    this.rolesForAllResources = new HashMap<>(builder.permissions.size());
    builder.permissions.forEach(
        (action, subjects) -> {
          this.rolesForAllResources.put(action, new HashSet<>(subjects));
        });
  }

  public static Permissions.Builder builder() {
    return new Builder();
  }

  private Set<String> rolesByResource(String resource) {
    return rolesForAllResources.getOrDefault(resource, Collections.emptySet());
  }

  private static boolean isInRole(Profile profile, Set<String> roles) {
    return roles.contains(profile.claim("roles"));
  }

  @Override
  public Mono<Profile> authorize(Profile profile, String resource) {
    return Mono.just(profile)
        .filter(p -> isInRole(p, rolesByResource(resource)))
        .switchIfEmpty(Mono.error(() -> new AccessControlException("Permission denied")));
  }
}
