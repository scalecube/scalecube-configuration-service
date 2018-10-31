package io.scalecube.configuration.repository.couchbase.admin;

import com.couchbase.client.java.cluster.AuthDomain;
import com.couchbase.client.java.cluster.UserRole;
import com.couchbase.client.java.cluster.UserSettings;
import io.scalecube.configuration.repository.couchbase.PasswordGenerator;
import java.util.stream.Collectors;

final class InsertUserOperation extends Operation<Boolean> {

  protected InsertUserOperation() {
  }

  @Override
  public Boolean execute(AdminOperationContext context) {
    context.cluster()
        .clusterManager()
        .upsertUser(
            AuthDomain.LOCAL,
            context.name(),
            UserSettings.build()
                .password(PasswordGenerator.md5Hash(context.name()))
                .name(context.name())
                .roles(
                    context.settings()
                        .bucketRoles()
                        .stream()
                        .map(role -> new UserRole(role, context.name()))
                        .collect(Collectors.toList())));
    return true;
  }
}
