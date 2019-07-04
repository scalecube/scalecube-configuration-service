package io.scalecube.configuration.authorization;

import io.scalecube.account.api.Role;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.security.api.Authorizer;

public class DefaultPermissions {

  public static final Authorizer PERMISSIONS =
      Permissions.builder()
          .grant(ConfigurationService.CONFIG_CREATE_REPO, Role.Owner)
          .grant(ConfigurationService.CONFIG_CREATE_ENTRY, Role.Owner, Role.Admin)
          .grant(ConfigurationService.CONFIG_UPDATE_ENTRY, Role.Owner, Role.Admin)
          .grant(ConfigurationService.CONFIG_DELETE_ENTRY, Role.Owner, Role.Admin)
          .grant(ConfigurationService.CONFIG_READ_ENTRY, Role.Owner, Role.Admin, Role.Member)
          .grant(
              ConfigurationService.CONFIG_READ_ENTRY_HISTORY, Role.Owner, Role.Admin, Role.Member)
          .grant(ConfigurationService.CONFIG_READ_LIST, Role.Owner, Role.Admin, Role.Member)
          .build();
}
