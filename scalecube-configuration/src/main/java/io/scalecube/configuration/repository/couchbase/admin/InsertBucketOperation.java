package io.scalecube.configuration.repository.couchbase.admin;

import com.couchbase.client.java.cluster.BucketSettings;
import com.couchbase.client.java.cluster.DefaultBucketSettings;
import reactor.core.publisher.Mono;
import rx.RxReactiveStreams;

final class InsertBucketOperation extends Operation<Mono<BucketSettings>> {

  @Override
  public Mono<BucketSettings> execute(AdminOperationContext context) {
    return Mono.from(
        RxReactiveStreams.toPublisher(
            context
                .cluster()
                .clusterManager()
                .flatMap(
                    clusterManager -> clusterManager.insertBucket(buildBucketSettings(context)))));
  }

  private DefaultBucketSettings buildBucketSettings(AdminOperationContext context) {
    return DefaultBucketSettings.builder()
        .name(context.name())
        .type(context.settings().bucketType())
        .quota(context.settings().bucketQuota())
        .replicas(context.settings().bucketReplicas())
        .indexReplicas(context.settings().bucketIndexReplicas())
        .enableFlush(context.settings().bucketEnableFlush())
        .build();
  }
}
