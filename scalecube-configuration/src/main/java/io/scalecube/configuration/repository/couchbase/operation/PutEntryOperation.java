package io.scalecube.configuration.repository.couchbase.operation;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.RawJsonDocument;
import io.scalecube.configuration.repository.Document;
import io.scalecube.configuration.repository.RepositoryEntryKey;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

final class PutEntryOperation extends EntryOperation {

  protected PutEntryOperation() {
  }

  @Override
  public List<Document> execute(OperationContext context) {
    return Collections.singletonList(put(context));
  }

  private Document put(OperationContext context) {
    Objects.requireNonNull(context.key());
    Objects.requireNonNull(context.document());

    logger.debug(
        "enter: put -> key = [{}], document = [{}]",
        context.key(),
        context.document());

    RepositoryEntryKey key = context.key();
    Document document = context.document();

    try {
      Bucket bucket = openBucket(context);
      bucket.upsert(RawJsonDocument.create(key.key(), encode(document)));
    } catch (Throwable throwable) {
      String message =
          String.format("Failed to put key: '%s'", key);
      handleException(throwable, message);
    }

    logger.debug(
        "exit: put -> key = [{}], document = [{}]",
        key,
        document);

    return document;
  }
}
