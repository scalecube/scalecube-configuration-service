package io.scalecube.configuration.operation;

import io.scalecube.configuration.api.Acknowledgment;
import io.scalecube.configuration.api.BadRequest;
import io.scalecube.configuration.api.SaveRequest;
import io.scalecube.configuration.repository.Document;
import io.scalecube.configuration.repository.RepositoryEntryKey;

import io.scalecube.security.Profile;

import java.util.UUID;

final class SaveEntry extends ServiceOperation<SaveRequest, Acknowledgment> {
  protected SaveEntry() {
  }

  @Override
  protected void validate(SaveRequest request) {
    super.validate(request);
    if (request.repository() == null || request.repository().length() == 0) {
      throw new BadRequest("Repository name is a required argument");
    }

    if (request.key() == null) {
      throw new BadRequest("Key is a required argument");
    }

    if (request.value() == null) {
      throw new BadRequest("Value is a required argument");
    }
  }

  @Override
  protected Acknowledgment process(SaveRequest request,
                                   Profile profile,
                                   ServiceOperationContext context) {
    Document document = Document.builder()
        .id(UUID.randomUUID().toString())
        .key(request.key())
        .value(request.value())
        .build();
    RepositoryEntryKey key = key(profile, request, request.key());
    context.dataAccess().put(key, document);
    return new Acknowledgment();
  }
}
