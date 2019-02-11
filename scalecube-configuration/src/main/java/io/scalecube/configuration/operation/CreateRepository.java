package io.scalecube.configuration.operation;

import io.scalecube.configuration.api.Acknowledgment;
import io.scalecube.configuration.api.BadRequest;
import io.scalecube.configuration.api.CreateRepositoryRequest;
import io.scalecube.security.Profile;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

final class CreateRepository extends ServiceOperation<CreateRepositoryRequest, Acknowledgment> {

  @Override
  protected void validate(CreateRepositoryRequest request) {
    super.validate(request);

    if (request.repository() == null || request.repository().length() == 0) {
      throw new BadRequest("Repository name is a required argument");
    }
  }

  @Override
  protected Publisher<Acknowledgment> process(
      CreateRepositoryRequest request, Profile profile, ServiceOperationContext context) {
    return Mono.fromCallable(() -> repository(profile, request))
        .flatMap(repository -> context.dataAccess().createRepository(repository))
        .thenReturn(new Acknowledgment());
  }
}
