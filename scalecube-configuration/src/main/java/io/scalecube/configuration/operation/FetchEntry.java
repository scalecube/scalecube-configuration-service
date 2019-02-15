package io.scalecube.configuration.operation;

import io.scalecube.configuration.api.BadRequest;
import io.scalecube.configuration.api.FetchRequest;
import io.scalecube.configuration.api.FetchResponse;
import io.scalecube.security.Profile;
import reactor.core.publisher.Mono;

final class FetchEntry extends ServiceOperation<FetchRequest, FetchResponse> {

  @Override
  protected void validate(FetchRequest request) {
    super.validate(request);

    if (request.repository() == null || request.repository().length() == 0) {
      throw new BadRequest("Repository name is a required argument");
    }

    if (request.key() == null || request.key().length() == 0) {
      throw new BadRequest("Key name is a required argument");
    }
  }

  @Override
  protected Mono<FetchResponse> process(
      FetchRequest request, Profile profile, ServiceOperationContext context) {
    return Mono.fromCallable(() -> key(profile, request, request.key()))
        .flatMap(key -> context.dataAccess().get(key))
        .map(document -> new FetchResponse(request.key(), document.value()));
  }
}
