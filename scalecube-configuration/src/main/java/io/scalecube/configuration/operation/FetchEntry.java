package io.scalecube.configuration.operation;

import io.scalecube.configuration.api.BadRequest;
import io.scalecube.configuration.api.FetchRequest;
import io.scalecube.configuration.api.FetchResponse;
import io.scalecube.configuration.repository.Document;

import io.scalecube.security.Profile;

final class FetchEntry extends ServiceOperation<FetchRequest, FetchResponse> {
  protected FetchEntry() {
  }


  @Override
  protected void validate(FetchRequest request) throws Throwable {
    super.validate(request);
    if (request.repository() == null || request.repository().length() == 0) {
      throw new BadRequest("Repository name is a required argument");
    }

    if (request.key() == null || request.key().length() == 0) {
      throw new BadRequest("Key name is a required argument");
    }
  }

  @Override
  protected FetchResponse process(FetchRequest request,
                                  Profile profile,
                                  ServiceOperationContext context) {
    Document entry = context.dataAccess().get(key(profile, request, request.key()));
    return new FetchResponse(request.key(), entry.value());
  }


}
