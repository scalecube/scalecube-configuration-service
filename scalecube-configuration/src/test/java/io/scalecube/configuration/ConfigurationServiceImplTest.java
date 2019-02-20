package io.scalecube.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.scalecube.configuration.api.Acknowledgment;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.CreateRepositoryRequest;
import io.scalecube.configuration.authorization.DefaultPermissions;
import io.scalecube.configuration.authorization.Role;
import io.scalecube.configuration.repository.InMemoryDataAccess;
import io.scalecube.configuration.repository.exception.RepositoryAlreadyExistsException;
import io.scalecube.security.acl.DefaultAccessControl;
import io.scalecube.security.api.Profile;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class ConfigurationServiceImplTest {
  private Profile owner;

  private Profile member;

  private Profile admin;

  @BeforeAll
  static void setUp() {
    StepVerifier.setDefaultTimeout(Duration.ofSeconds(3));
  }

  private ConfigurationService createService() {
    this.owner = createProfile(Role.Owner);
    this.member = createProfile(Role.Member);
    this.admin = createProfile(Role.Admin);

    return createService(owner);
  }

  private Profile createProfile(Role role) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("roles", role.toString());
    return Profile.builder().tenant("myorg").claims(claims).build();
  }

  private ConfigurationService createService(Profile profile) {
    return new ConfigurationServiceImpl(
        new InMemoryDataAccess(),
        DefaultAccessControl.builder()
            .authenticator(new DummyAuthenticator(profile))
            .authorizer(DefaultPermissions.PERMISSIONS)
            .build());
  }

  @Test
  void shouldCreateRepository() {
    ConfigurationService service = createService();
    StepVerifier.create(
            service.createRepository(new CreateRepositoryRequest(new Object(), "myrepo")))
        .expectSubscription()
        .assertNext(r -> assertEquals(Acknowledgment.class, r.getClass()))
        .verifyComplete();
  }

  @Test
  void shouldFailWithRepositoryAlreadyExists() {
    ConfigurationService service = createService();
    StepVerifier.create(
            service.createRepository(new CreateRepositoryRequest(new Object(), "myrepo")))
        .expectSubscription()
        .assertNext(r -> assertEquals(Acknowledgment.class, r.getClass()))
        .verifyComplete();

    StepVerifier.create(
            service.createRepository(new CreateRepositoryRequest(new Object(), "myrepo")))
        .expectSubscription()
        .expectError(RepositoryAlreadyExistsException.class)
        .verify();
  }
}
