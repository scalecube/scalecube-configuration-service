package io.scalecube.configuration.repository.couchbase;

import io.scalecube.configuration.repository.exception.InvalidRepositoryNameException;
import java.util.Objects;

class ConfigurationBucketName {
  private static final CouchbaseSettings settings = new CouchbaseSettings.Builder().build();
  private final String name;

  private ConfigurationBucketName(String name) {
    this.name = name;
  }

  String name() {
    return name;
  }

  static ConfigurationBucketName from(String namespace, String name) {
    String bucketName = getBucketName(namespace, name);
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

  private static String getBucketName(String namespace, String repository) {
    Objects.requireNonNull(namespace, "namespace");
    Objects.requireNonNull(repository, "repository");
    return String.format(settings.bucketNamePattern(), namespace, repository);
  }
}
