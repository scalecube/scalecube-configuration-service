package io.scalecube.configuration.repository.couchbase.admin;

import com.couchbase.client.java.cluster.AsyncClusterManager;
import com.couchbase.client.java.cluster.BucketSettings;
import reactor.core.publisher.Flux;
import rx.RxReactiveStreams;

final class ListBucketNamesOperation extends Operation<Flux<String>> {

  @Override
  public Flux<String> execute(AdminOperationContext context) {
    return Flux.from(
        RxReactiveStreams.toPublisher(
            context
                .cluster()
                .clusterManager()
                .flatMap(AsyncClusterManager::getBuckets)
                .map(BucketSettings::name)));
  }
}
