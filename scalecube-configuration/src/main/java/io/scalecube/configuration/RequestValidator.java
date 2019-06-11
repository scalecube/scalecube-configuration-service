package io.scalecube.configuration;

import io.scalecube.configuration.api.CreateRepositoryRequest;
import io.scalecube.configuration.api.DeleteEntryRequest;
import io.scalecube.configuration.api.ReadListRequest;
import io.scalecube.configuration.api.ReadEntryRequest;
import io.scalecube.configuration.api.CreateEntryRequest;
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
        });
  }

  static Mono<Void> validate(ReadListRequest request) {
    return Mono.fromRunnable(
        () -> {
          validateToken(request.apiKey());
          validateRepository(request.repository());
        });
  }

  static Mono<Void> validate(CreateEntryRequest request) {
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
  }

  private static void validateToken(Object token) {
    if (token == null || token.toString().trim().isEmpty()) {
      throw new IllegalArgumentException("Please specify 'APIKey'");
    }
  }

  private static void validateKey(String key) {
    if (key == null || key.trim().isEmpty()) {
      throw new IllegalArgumentException("Please specify 'key'");
    }
  }
}
