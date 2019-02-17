package io.scalecube.configuration.operation;

import io.scalecube.configuration.api.Acknowledgment;
import io.scalecube.configuration.api.BadRequest;
import io.scalecube.configuration.api.DeleteRequest;
import io.scalecube.security.api.Profile;
import reactor.core.publisher.Mono;

final class DeleteEntry extends ServiceOperation<DeleteRequest, Acknowledgment> {

  @Override
  protected void validate(DeleteRequest request) {
    super.validate(request);

    if (request.repository() == null || request.repository().length() == 0) {
      throw new BadRequest("Repository name is a required argument");
    }

    if (request.key() == null) {
      throw new BadRequest("Key is a required argument");
    }
  }

  @Override
  protected Mono<Acknowledgment> process(
      DeleteRequest request, Profile profile, ServiceOperationContext context) {
    return Mono.fromCallable(() -> key(profile, request, request.key()))
        .flatMap(key -> context.dataAccess().remove(key))
        .thenReturn(new Acknowledgment());
  }
}
