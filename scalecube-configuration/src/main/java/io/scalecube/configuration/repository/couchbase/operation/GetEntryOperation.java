package io.scalecube.configuration.repository.couchbase.operation;

import com.couchbase.client.java.Bucket;
import io.scalecube.configuration.repository.Document;
import io.scalecube.configuration.repository.RepositoryEntryKey;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

final class GetEntryOperation extends EntryOperation {

  protected GetEntryOperation() {
  }

  @Override
  public List<Document> execute(OperationContext context) {
    return Collections.singletonList(get(context));
  }

  private Document get(OperationContext context) {
    Objects.requireNonNull(context.key());
    logger.debug("enter: get -> key = [{}]", context.key());

    RepositoryEntryKey key = context.key();

    Bucket bucket;
    Document document = null;

    try {
      bucket = openBucket(context);
      document = getDocument(bucket, key.key());
    } catch (Throwable ex) {
      String message =
          String.format("Failed to get key: '%s'", key);
      handleException(ex, message);
    }

    logger.debug(
        "exit: get key -> [ {} ], return -> [ {} ]",
        key,
        document != null ? document.value() : null);

    return document;
  }
}