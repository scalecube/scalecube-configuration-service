package io.scalecube.configuration.repository.couchbase.admin;

import com.couchbase.client.java.cluster.BucketSettings;
import java.util.List;
import java.util.stream.Collectors;

final class ListBucketNamesOperation extends Operation<List<String>> {

  protected ListBucketNamesOperation() {
  }

  @Override
  public List<String> execute(AdminOperationContext context) {
    return context.cluster()
        .clusterManager()
        .getBuckets()
        .stream()
        .map(BucketSettings::name)
        .collect(Collectors.toList());
  }
}
