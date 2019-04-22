package io.scalecube.configuration;

import io.scalecube.configuration.api.CreateRepositoryRequest;
import io.scalecube.configuration.api.DeleteRequest;
import io.scalecube.configuration.api.EntriesRequest;
import io.scalecube.configuration.api.FetchRequest;
import io.scalecube.configuration.api.SaveRequest;
import reactor.core.publisher.Mono;

final class RequestValidator {

  static Mono<Void> validate(CreateRepositoryRequest request) {
    return Mono.fromRunnable(
        () -> {
          validateToken(request.token());
          validateRepository(request.repository());
        });
  }

  static Mono<Void> validate(FetchRequest request) {
    return Mono.fromRunnable(
        () -> {
          validateToken(request.token());
          validateRepository(request.repository());
          validateKey(request.key());
        });
  }

  static Mono<Void> validate(EntriesRequest request) {
    return Mono.fromRunnable(
        () -> {
          validateToken(request.token());
          validateRepository(request.repository());
        });
  }

  static Mono<Void> validate(SaveRequest request) {
    return Mono.fromRunnable(
        () -> {
          validateToken(request.token());
          validateRepository(request.repository());
          validateKey(request.key());
        });
  }

  static Mono<Void> validate(DeleteRequest request) {
    return Mono.fromRunnable(
        () -> {
          validateToken(request.token());
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
      throw new IllegalArgumentException("Please specify 'token'");
    }
  }

  private static void validateKey(String key) {
    if (key == null || key.trim().isEmpty()) {
      throw new IllegalArgumentException("Please specify 'key'");
    }
  }
}
