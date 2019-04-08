package io.scalecube.configuration.repository.couchbase.admin;

import com.couchbase.client.java.document.RawJsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import reactor.core.publisher.Mono;
import rx.Observable;
import rx.RxReactiveStreams;

final class InsertDocOperation extends Operation<Mono<Boolean>> {

  @Override
  public Mono<Boolean> execute(AdminOperationContext context) {
    return Mono.from(
        RxReactiveStreams.toPublisher(
            context
                .cluster()
                .openBucket(context.name())
                .flatMap(asyncBucket -> asyncBucket
                    .upsert(RawJsonDocument
                        .create(jsonId(context), jsonContent(context))))
                .flatMap(rawJsonDocument -> Observable.just(rawJsonDocument.id() != null))
        ))
        .onErrorMap(throwable -> new Exception(throwable.getMessage()));
  }

  private String jsonId(AdminOperationContext context) {
    return context.orgId() + "." + context.repoName();
  }

  private String jsonContent(AdminOperationContext context) {
    String content =
        JsonObject.create()
            .put("org_id", context.orgId())
            .put("repo_name", context.repoName())
            .put("entities", JsonArray.empty())
            .toString();
    return content;
  }
}
