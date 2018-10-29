package io.scalecube.configuration.repository.couchbase;

import io.scalecube.configuration.repository.Repository;
import io.scalecube.configuration.repository.exception.InvalidRepositoryNameException;
import java.util.Objects;

class ConfigurationBucketName {
  private final String name;

  private ConfigurationBucketName(String name) {
    this.name = name;
  }

  String name() {
    return name;
  }

  static ConfigurationBucketName from(Repository repository, CouchbaseSettings settings) {
    String bucketName = getBucketName(repository.namespace(), repository.name(), settings);
    validateBucketName(bucketName);
    return new ConfigurationBucketName(bucketName);
  }

  private static void validateBucketName(String name) {
    if (name == null || name.length() == 0) {
      throw new InvalidRepositoryNameException("name be empty");
    }

    if (!name.matches("^[.%a-zA-Z0-9_-]*$")) {
      throw new InvalidRepositoryNameException(
          "name can only contain characters in range A-Z, a-z, 0-9 as well as "
              + "underscore, period, dash & percent.");
    }
  }

  private static String getBucketName(
      String namespace, String repository, CouchbaseSettings settings) {
    Objects.requireNonNull(namespace, "namespace");
    Objects.requireNonNull(repository, "name");
    return String.format(settings.bucketNamePattern(), namespace, repository);
  }
}
