package io.scalecube.configuration;

import io.scalecube.configuration.api.CreateOrUpdateEntryRequest;
import io.scalecube.configuration.api.CreateRepositoryRequest;
import io.scalecube.configuration.api.DeleteEntryRequest;
import io.scalecube.configuration.api.ReadEntryHistoryRequest;
import io.scalecube.configuration.api.ReadEntryRequest;
import io.scalecube.configuration.api.ReadListRequest;
import reactor.core.publisher.Mono;

final class RequestValidator {

  static Mono<Void> validate(CreateRepositoryRequest request) {
    return Mono.fromRunnable(
        () -> {
          validateToken(request.apiKey());
          validateRepository(request.repository());
        });
  }

  static Mono<Void> validate(ReadEntryRequest request) {
    return Mono.fromRunnable(
        () -> {
          validateToken(request.apiKey());
          validateRepository(request.repository());
          validateKey(request.key());
          validateVersion(request.version());
        });
  }

  static Mono<Void> validate(ReadListRequest request) {
    return Mono.fromRunnable(
        () -> {
          validateToken(request.apiKey());
          validateRepository(request.repository());
          validateVersion(request.version());
        });
  }

  static Mono<Void> validate(ReadEntryHistoryRequest request) {
    return Mono.fromRunnable(
        () -> {
          validateToken(request.apiKey());
          validateRepository(request.repository());
          validateKey(request.key());
        });
  }

  static Mono<Void> validate(CreateOrUpdateEntryRequest request) {
    return Mono.fromRunnable(
        () -> {
          validateToken(request.apiKey());
          validateRepository(request.repository());
          validateKey(request.key());
        });
  }

  static Mono<Void> validate(DeleteEntryRequest request) {
    return Mono.fromRunnable(
        () -> {
          validateToken(request.apiKey());
          validateRepository(request.repository());
          validateKey(request.key());
        });
  }

  private static void validateRepository(String repository) {
    if (repository == null || repository.trim().isEmpty()) {
      throw new IllegalArgumentException("Please specify 'repository'");
    }
    if (repository.contains("::")) {
      throw new IllegalArgumentException("Repository name should not contains substring '::'");
    }
  }

  private static void validateToken(Object token) {
    if (token == null || token.toString().trim().isEmpty()) {
      throw new IllegalArgumentException("Please specify 'apiKey'");
    }
  }

  private static void validateKey(String key) {
    if (key == null || key.trim().isEmpty()) {
      throw new IllegalArgumentException("Please specify 'key'");
    }
  }

  private static void validateVersion(Object version) {
    try {
      if (version == null
          || version instanceof Integer && ((Integer) version) > 0
          || version instanceof Long && ((Long) version) > 0
          || version instanceof String && Integer.valueOf((String) version) > 0) {
        return;
      }
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException("Version must be a positive number");
    }
    throw new IllegalArgumentException("Version must be a positive number");
  }
}
