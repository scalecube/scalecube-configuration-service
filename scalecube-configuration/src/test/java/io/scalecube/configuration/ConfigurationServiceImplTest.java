package io.scalecube.configuration;

import static org.junit.jupiter.api.Assertions.*;

import io.scalecube.configuration.api.BadRequest;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.CreateRepositoryRequest;
import io.scalecube.configuration.api.InvalidAuthenticationToken;
import io.scalecube.configuration.api.InvalidPermissionsException;
import io.scalecube.configuration.repository.inmem.InMemoryDataAccess;
import io.scalecube.security.Profile;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class ConfigurationServiceImplTest {

  @Test
  void create_repository_null_request_should_fail_with_IllegalArgumentException() {
    ConfigurationService service = createService(new ProfileBuilder().build());
    Duration duration = StepVerifier
        .create(
            service.createRepository(null))
        .expectSubscription()
        .expectError(BadRequest.class)
        .verify();
    assertNotNull(duration);
  }


  @Test
  void create_repository_null_repository_should_fail_with_IllegalArgumentException() {
    ConfigurationService service = createService(null);
    Duration duration = StepVerifier
        .create(
            service.createRepository(new CreateRepositoryRequest(null, null)))
        .expectSubscription()
        .expectError(BadRequest.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void create_repository_null_token_should_fail_with_IllegalArgumentException() {
    ConfigurationService service = createService(null);
    Duration duration = StepVerifier
        .create(
            service.createRepository(new CreateRepositoryRequest(null, "myrepo")))
        .expectSubscription()
        .expectError(BadRequest.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void create_repository_null_profile_should_fail_with_InvalidAuthenticationToken() {
    ConfigurationService service = createService(null);
    Duration duration = StepVerifier
        .create(
            service.createRepository(new CreateRepositoryRequest(new Object(), "myrepo")))
        .expectSubscription()
        .expectError(InvalidAuthenticationToken.class)
        .verify();
    assertNotNull(duration);
  }


  @Test
  void create_repository_null_tenant_should_fail_with_InvalidAuthenticationToken() {
    ConfigurationService service = createService(new ProfileBuilder().build());
    Duration duration = StepVerifier
        .create(
            service.createRepository(new CreateRepositoryRequest(new Object(), "myrepo")))
        .expectSubscription()
        .expectError(InvalidAuthenticationToken.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void create_repository_null_claims_should_fail_with_InvalidAuthenticationToken() {
    ConfigurationService service = createService(new ProfileBuilder().tenant("myorg").build());
    Duration duration = StepVerifier
        .create(
            service.createRepository(new CreateRepositoryRequest(new Object(), "myrepo")))
        .expectSubscription()
        .expectError(InvalidAuthenticationToken.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void create_repository_null_role_should_fail_with_InvalidAuthenticationToken() {
    ConfigurationService service = createService(new ProfileBuilder().tenant("myorg")
        .claims(new HashMap<>()).build());
    Duration duration = StepVerifier
        .create(
            service.createRepository(new CreateRepositoryRequest(new Object(), "myrepo")))
        .expectSubscription()
        .expectError(InvalidAuthenticationToken.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void create_repository_member_role_should_fail_with_InvalidPermissionsException() {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", Role.Member);
    ConfigurationService service = createService(new ProfileBuilder().tenant("myorg")
        .claims(claims).build());
    Duration duration = StepVerifier
        .create(
            service.createRepository(new CreateRepositoryRequest(new Object(), "myrepo")))
        .expectSubscription()
        .expectError(InvalidPermissionsException.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void createRepository() {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", Role.Owner);
    ConfigurationService service = createService(new ProfileBuilder().tenant("myorg")
        .claims(claims).build());
    Duration duration = StepVerifier
        .create(
            service.createRepository(new CreateRepositoryRequest(new Object(), "myrepo")))
        .expectSubscription()
        .assertNext(Assertions::assertNotNull)
        .verifyComplete();
    assertNotNull(duration);
  }

  private ConfigurationService createService(Profile profile) {
    return ConfigurationServiceImpl.builder()
        .dataAccess(new InMemoryDataAccess())
        .tokenVerifier((token) -> profile)
        .build();
  }

  @Test
  void fetch() {
  }

  @Test
  void entries() {
  }

  @Test
  void save() {
  }

  @Test
  void delete() {
  }

  class ProfileBuilder {
    private String id;
    private String tenant;
    private String email;
    private boolean isEmaildVerified;
    private String name;
    private String familyName;
    private String givenName;
    private Map<String, Object> claims;

    Profile build() {
      return new Profile(id, tenant, email, isEmaildVerified, name, familyName, givenName, claims);
    }

    ProfileBuilder id(String id) {
      this.id = id;
      return this;
    }


    ProfileBuilder tenant(String tenant) {
      this.tenant = tenant;
      return this;
    }

    ProfileBuilder email(String email) {
      this.email = email;
      return this;
    }

    ProfileBuilder emailVerified(boolean emailVerified) {
      this.isEmaildVerified = emailVerified;
      return this;
    }

    ProfileBuilder name(String name) {
      this.name = name;
      return this;
    }

    ProfileBuilder givenName(String name) {
      this.givenName = name;
      return this;
    }

    ProfileBuilder familyName(String name) {
      this.familyName = name;
      return this;
    }

    ProfileBuilder claims(Map<String, Object> claims) {
      this.claims = claims;
      return this;
    }
  }
}