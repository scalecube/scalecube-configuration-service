package io.scalecube.configuration.repository.couchbase.operation;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import io.scalecube.configuration.repository.Document;
import io.scalecube.configuration.repository.RepositoryEntryKey;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

final class RemoveEntryOperation extends EntryOperation {

  protected RemoveEntryOperation() {
  }

  @Override
  public List<Document> execute(OperationContext context) {
    return Collections.singletonList(remove(context));
  }

  private Document remove(OperationContext context) {
    RepositoryEntryKey key = context.key();
    logger.debug(
        "enter: remove -> key = [{}]",
        key);

    Objects.requireNonNull(key);
    Document document = null;

    try {
      Bucket bucket = openBucket(context);
      JsonDocument jsonDocument = bucket.remove(key.key());
      document = Document.builder()
          .id(jsonDocument.id())
          .key(key.key())
          .build();
    } catch (Throwable throwable) {
      String message =
          String.format("Failed to remove cluster: '%s'", key);
      handleException(throwable, message);
    }

    logger.debug(
        "exit: remove -> key = [{}]",
        key);
    return document;
  }


}
