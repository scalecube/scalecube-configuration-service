package io.scalecube.configuration.repository.couchbase.admin;

import com.couchbase.client.java.cluster.AuthDomain;
import com.couchbase.client.java.cluster.UserRole;
import com.couchbase.client.java.cluster.UserSettings;
import io.scalecube.configuration.repository.couchbase.PasswordGenerator;
import java.util.stream.Collectors;
import reactor.core.publisher.Mono;
import rx.RxReactiveStreams;

final class InsertUserOperation extends Operation<Mono<Boolean>> {

  @Override
  public Mono<Boolean> execute(AdminOperationContext context) {
    return Mono.from(
        RxReactiveStreams.toPublisher(
            context
                .cluster()
                .clusterManager()
                .flatMap(
                    clusterManager ->
                        clusterManager.upsertUser(
                            AuthDomain.LOCAL, context.name(), buildUserSettings(context)))));
  }

  private UserSettings buildUserSettings(AdminOperationContext context) {
    return UserSettings.build()
        .password(PasswordGenerator.md5Hash(context.name()))
        .name(context.name())
        .roles(
            context.settings().bucketRoles().stream()
                .map(role -> new UserRole(role, context.name()))
                .collect(Collectors.toList()));
  }
}
