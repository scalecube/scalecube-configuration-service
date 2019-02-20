package io.scalecube.configuration.authorization;

import io.scalecube.account.api.Role;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.security.api.Authorizer;

public class DefaultPermissions {

  public static final Authorizer PERMISSIONS =
      Permissions.builder()
          .grant(ConfigurationService.CONFIG_CREATE_REPO, Role.Owner, Role.Admin)
          .grant(ConfigurationService.CONFIG_SAVE, Role.Owner, Role.Admin)
          .grant(ConfigurationService.CONFIG_DELETE, Role.Owner, Role.Admin)
          .grant(ConfigurationService.CONFIG_FETCH, Role.Owner, Role.Admin, Role.Member)
          .grant(ConfigurationService.CONFIG_ENTRIES, Role.Owner, Role.Admin, Role.Member)
          .build();
}
