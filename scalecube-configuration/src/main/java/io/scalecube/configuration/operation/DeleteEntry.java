package io.scalecube.configuration.operation;

import io.scalecube.configuration.api.Acknowledgment;
import io.scalecube.configuration.api.BadRequest;
import io.scalecube.configuration.api.DeleteRequest;
import io.scalecube.configuration.repository.RepositoryEntryKey;
import io.scalecube.security.Profile;

final class DeleteEntry extends ServiceOperation<DeleteRequest, Acknowledgment> {

  DeleteEntry() {}

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
  protected Acknowledgment process(
      DeleteRequest request, Profile profile, ServiceOperationContext context) {
    RepositoryEntryKey key = key(profile, request, request.key());
    context.dataAccess().remove(key);
    return new Acknowledgment();
  }
}
