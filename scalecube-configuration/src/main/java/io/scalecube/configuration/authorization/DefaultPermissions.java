package io.scalecube.configuration.authorization;

import io.scalecube.account.api.Role;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.security.api.Authorizer;

public class DefaultPermissions {
  
  public static final Authorizer PERMISSIONS = Permissions.builder()
  .grant(
      ConfigurationService.CONFIG_CREATE_REPO,
      Role.Owner.toString(),
      Role.Admin.toString())
  .grant(
      ConfigurationService.CONFIG_SAVE,
      Role.Owner.toString(),
      Role.Admin.toString())
  .grant(
      ConfigurationService.CONFIG_DELETE,
      Role.Owner.toString(),
      Role.Admin.toString())
  .grant(
      ConfigurationService.CONFIG_FETCH,
      Role.Owner.toString(),
      Role.Admin.toString(),
      Role.Member.toString())
  .grant(
      ConfigurationService.CONFIG_ENTRIES,
      Role.Owner.toString(),
      Role.Admin.toString(),
      Role.Member.toString())
  .build();
  
}
