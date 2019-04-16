package io.scalecube.configuration.repository.couchbase;

import java.util.List;

public final class CouchbaseSettings {

  private List<String> hosts;
  private String username;
  private String password;
  private String bucketName;

  public List<String> hosts() {
    return hosts;
  }

  public String username() {
    return username;
  }

  public String password() {
    return password;
  }

  public String bucketName() {
    return bucketName;
  }
}
