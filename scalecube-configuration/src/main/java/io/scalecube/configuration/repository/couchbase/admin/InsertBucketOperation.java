package io.scalecube.configuration.repository.couchbase.admin;

import com.couchbase.client.java.cluster.BucketSettings;
import com.couchbase.client.java.cluster.DefaultBucketSettings.Builder;

final class InsertBucketOperation extends Operation<BucketSettings> {

  protected InsertBucketOperation() {
  }

  @Override
  public BucketSettings execute(AdminOperationContext context) {
    return context.cluster()
        .clusterManager()
        .insertBucket(
            new Builder()
                .type(context.settings().bucketType())
                .name(context.name())
                .quota(context.settings().bucketQuota()) // megabytes
                .replicas(context.settings().bucketReplicas())
                .indexReplicas(context.settings().bucketIndexReplicas())
                .enableFlush(context.settings().bucketEnableFlush())
                .build());
  }
}
