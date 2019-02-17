package io.scalecube.configuration.operation;

import io.scalecube.configuration.api.BadRequest;
import io.scalecube.configuration.api.FetchRequest;
import io.scalecube.configuration.api.FetchResponse;
import io.scalecube.security.api.Profile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

final class FetchEntries extends ServiceOperation<FetchRequest, FetchResponse> {

  @Override
  protected void validate(FetchRequest request) {
    super.validate(request);

    if (request.repository() == null || request.repository().length() == 0) {
      throw new BadRequest("Repository name is a required argument");
    }
  }

  @Override
  protected Flux<FetchResponse> process(
      FetchRequest request, Profile profile, ServiceOperationContext context) {
    return Mono.fromCallable(() -> repository(profile, request))
        .flatMapMany(repository -> context.dataAccess().entries(repository))
        .map(document -> new FetchResponse(document.key(), document.value()));
  }

}
