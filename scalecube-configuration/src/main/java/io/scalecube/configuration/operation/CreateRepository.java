package io.scalecube.configuration.operation;

import io.scalecube.configuration.api.Acknowledgment;
import io.scalecube.configuration.api.BadRequest;
import io.scalecube.configuration.api.CreateRepositoryRequest;
import io.scalecube.security.Profile;

final class CreateRepository extends ServiceOperation<CreateRepositoryRequest, Acknowledgment> {

  CreateRepository() {}

  @Override
  protected void validate(CreateRepositoryRequest request) {
    super.validate(request);

    if (request.repository() == null || request.repository().length() == 0) {
      throw new BadRequest("Repository name is a required argument");
    }
  }

  @Override
  protected Acknowledgment process(
      CreateRepositoryRequest request, Profile profile, ServiceOperationContext context) {
    context.dataAccess().createRepository(repository(profile, request));
    return new Acknowledgment();
  }
}
