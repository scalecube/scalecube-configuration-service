package io.scalecube.configuration.operation;

import io.scalecube.configuration.api.BadRequest;
import io.scalecube.configuration.api.FetchRequest;
import io.scalecube.configuration.api.FetchResponse;
import io.scalecube.configuration.repository.Repository;
import io.scalecube.security.Profile;

final class FetchEntries extends ServiceOperation<FetchRequest, FetchResponse[]> {

  FetchEntries() {}

  @Override
  protected FetchResponse[] process(
      FetchRequest request, Profile profile, ServiceOperationContext context) {
    Repository repository = repository(profile, request);
    return context.dataAccess().entries(repository).stream()
        .map(doc -> FetchResponse.builder().key(doc.key()).value(doc.value()).build())
        .toArray(FetchResponse[]::new);
  }

  @Override
  protected void validate(FetchRequest request) {
    super.validate(request);
    if (request.repository() == null || request.repository().length() == 0) {
      throw new BadRequest("Repository name is a required argument");
    }
  }
}
