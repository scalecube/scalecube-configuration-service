package io.scalecube.configuration;

import io.scalecube.security.api.Authenticator;
import io.scalecube.security.api.Profile;
import reactor.core.publisher.Mono;

public class DummyAuthenticator implements Authenticator {

  final Profile profile;

  public DummyAuthenticator(Profile profile) {
    this.profile = profile;
  }

  @Override
  public Mono<Profile> authenticate(String token) {
    return Mono.just(profile);
  }
}
