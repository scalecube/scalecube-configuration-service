package io.scalecube.configuration;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.scalecube.configuration.api.BadRequest;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.CreateRepositoryRequest;
import io.scalecube.configuration.api.DeleteRequest;
import io.scalecube.configuration.api.FetchRequest;
import io.scalecube.configuration.api.InvalidAuthenticationToken;
import io.scalecube.configuration.api.InvalidPermissionsException;
import io.scalecube.configuration.api.SaveRequest;
import io.scalecube.configuration.repository.exception.DuplicateRepositoryException;
import io.scalecube.configuration.repository.exception.KeyNotFoundException;
import io.scalecube.configuration.repository.exception.RepositoryNotFoundException;
import io.scalecube.configuration.repository.inmem.InMemoryDataAccess;
import io.scalecube.configuration.tokens.TokenVerifierFactory;
import io.scalecube.security.Profile;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import reactor.test.StepVerifier;

public class ConfigurationServiceImplTest {
  private final ObjectMapper mapper = new ObjectMapper();
  String token = "eyJraWQiOiIzZjhiZTEzNS1kZjdhLTQ0ODQtYjEwZC01ZjlkYTZjNWZiNjMiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJPUkctNUYzRDAyNDQzNjFEMzE5MjRFOUQiLCJpYXQiOjE1MzUwMzU5MjgsInN1YiI6Ik9SRy01RjNEMDI0NDM2MUQzMTkyNEU5RCIsImlzcyI6InNjYWxlY3ViZS5pbyIsImF1ZCI6Im15VGVzdE9yZzUiLCJyb2xlIjoiT3duZXIiLCJleHAiOjMwNzI3NTAyNTZ9.kKlBJZZeM5YctGS-WlFHATLofQYOu4P0igh8tVYZtH8";

  @Test
  void create_repository_null_request_should_fail_withBadRequest() {
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
  void create_repository_null_repository_should_fail_withBadRequest() {
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
  void create_repository_null_token_should_fail_withBadRequest() {
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
  void create_duplicate_repository_should_fail_with_DuplicateRepositoryException() {
    ConfigurationService service = createService();
    StepVerifier
        .create(
            service.createRepository(new CreateRepositoryRequest(new Object(), "myrepo")))
        .expectSubscription()
        .assertNext(Assertions::assertNotNull)
        .verifyComplete();
    Duration duration = StepVerifier
        .create(
            service.createRepository(new CreateRepositoryRequest(new Object(), "myrepo")))
        .expectSubscription()
        .expectError(DuplicateRepositoryException.class)
        .verify();
    assertNotNull(duration);
  }

  private ConfigurationService createService() {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", Role.Owner);
    return createService(new ProfileBuilder().tenant("myorg")
        .claims(claims).build());
  }

  @Test
  void createRepository() {
    ConfigurationService service = createService();
    Duration duration = createRepository(service);
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
    ConfigurationService service = createService();
    createRepository(service);
    StepVerifier
        .create(
            service.save(new SaveRequest(new Object(), "myrepo", "mykey",
                mapper.valueToTree(1))))
        .expectSubscription()
        .assertNext(Assertions::assertNotNull)
        .verifyComplete();
    Duration duration = StepVerifier
        .create(
            service.fetch(new FetchRequest(new Object(), "myrepo", "mykey")))
        .expectSubscription()
        .assertNext(result -> assertEquals(String.valueOf(result.value()), "1"))
        .verifyComplete();
    assertNotNull(duration);
  }

  @Test
  void fetch_null_request_should_fail_withBadRequest() {
    ConfigurationService service = createService(new ProfileBuilder().build());
    Duration duration = StepVerifier
        .create(
            service.fetch(null))
        .expectSubscription()
        .expectError(BadRequest.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void fetch_null_repository_should_fail_withBadRequest() {
    ConfigurationService service = createService(new ProfileBuilder().build());
    Duration duration = StepVerifier
        .create(
            service.fetch(new FetchRequest(new Object()  , null, "mykey")))
        .expectSubscription()
        .expectError(BadRequest.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void fetch_null_token_should_fail_withBadRequest() {
    ConfigurationService service = createService(new ProfileBuilder().build());
    Duration duration = StepVerifier
        .create(
            service.fetch(new FetchRequest(null, "myrepo", "mykey")))
        .expectSubscription()
        .expectError(BadRequest.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void fetch_null_profile_should_fail_with_InvalidAuthenticationToken() {
    ConfigurationService service = createService(null);
    Duration duration = StepVerifier
        .create(
            service.fetch(new FetchRequest(new Object()  , "myrepo", "mykey")))
        .expectSubscription()
        .expectError(InvalidAuthenticationToken.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void fetch_null_tenant_should_fail_with_InvalidAuthenticationToken() {
    ConfigurationService service = createService(new ProfileBuilder().build());
    Duration duration = StepVerifier
        .create(
            service.fetch(new FetchRequest(new Object()  , "myrepo", "mykey")))
        .expectSubscription()
        .expectError(InvalidAuthenticationToken.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void fetch_null_claims_should_fail_with_InvalidAuthenticationToken() {
    ConfigurationService service = createService(new ProfileBuilder().tenant("myorg").build());
    Duration duration = StepVerifier
        .create(
            service.fetch(new FetchRequest(new Object()  , "myrepo", "mykey")))
        .expectSubscription()
        .expectError(InvalidAuthenticationToken.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void fetch_null_role_should_fail_with_InvalidAuthenticationToken() {
    ConfigurationService service = createService(new ProfileBuilder().tenant("myorg")
        .claims(new HashMap<>()).build());
    Duration duration = StepVerifier
        .create(
            service.fetch(new FetchRequest(new Object()  , "myrepo", "mykey")))
        .expectSubscription()
        .expectError(InvalidAuthenticationToken.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void fetch_should_fail_with_RepositoryNotFoundException() {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", Role.Member);
    ConfigurationService service = createService(new ProfileBuilder().tenant("myorg")
        .claims(claims).build());
    Duration duration = StepVerifier
        .create(
            service.fetch(new FetchRequest(new Object()  , "myrepo", "mykey")))
        .expectSubscription()
        .expectError(RepositoryNotFoundException.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void fetch_null_key_should_fail_with_BadRequest() {
    ConfigurationService service = createService();
    createRepository(service);
    Duration duration = StepVerifier
        .create(
            service.fetch(new FetchRequest(new Object()  , "myrepo", null)))
        .expectSubscription()
        .expectError(BadRequest.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void fetch_should_fail_with_KeyNotFoundException() {
    ConfigurationService service = createService();
    createRepository(service);
    Duration duration = StepVerifier
        .create(
            service.fetch(new FetchRequest(new Object()  , "myrepo", "mykey")))
        .expectSubscription()
        .expectError(KeyNotFoundException.class)
        .verify();
    assertNotNull(duration);
  }

  private Duration createRepository(ConfigurationService service) {
    return StepVerifier
          .create(
              service.createRepository(
                  new CreateRepositoryRequest(new Object(), "myrepo")))
          .expectSubscription()
          .assertNext(Assertions::assertNotNull)
          .verifyComplete();
  }

  @Test
  void entries() {
    ConfigurationService service = createService();
    createRepository(service);
    StepVerifier
        .create(
            service.save(new SaveRequest(new Object(), "myrepo", "mykey",
                mapper.valueToTree(1))))
        .expectSubscription()
        .assertNext(Assertions::assertNotNull)
        .verifyComplete();

    StepVerifier
        .create(
            service.save(new SaveRequest(new Object(), "myrepo", "mykey2",
                mapper.valueToTree(2))))
        .expectSubscription()
        .assertNext(Assertions::assertNotNull)
        .verifyComplete();

    assertNotNull(StepVerifier
        .create(
            service.entries(new FetchRequest(new Object()  , "myrepo", null)))
        .expectSubscription()
        .assertNext(r -> {
          assertEquals(2, r.entries().length);
          assertTrue(Arrays.stream(r.entries()).anyMatch(i->Objects.equals(i.value().toString(),
              "1")));
          assertTrue(Arrays.stream(r.entries()).anyMatch(i->Objects.equals(i.value().toString(),
              "2")));
        })
        .verifyComplete());
  }

  @Test
  void entries_fetch_null_request_should_fail_withBadRequest() {
    ConfigurationService service = createService(new ProfileBuilder().build());
    Duration duration = StepVerifier
        .create(
            service.entries(null))
        .expectSubscription()
        .expectError(BadRequest.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void entries_null_repository_should_fail_withBadRequest() {
    ConfigurationService service = createService(new ProfileBuilder().build());
    Duration duration = StepVerifier
        .create(
            service.entries(new FetchRequest(new Object()  , null, "mykey")))
        .expectSubscription()
        .expectError(BadRequest.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void entries_null_token_should_fail_withBadRequest() {
    ConfigurationService service = createService(new ProfileBuilder().build());
    Duration duration = StepVerifier
        .create(
            service.entries(new FetchRequest(null, "myrepo", "mykey")))
        .expectSubscription()
        .expectError(BadRequest.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void entries_null_profile_should_fail_with_InvalidAuthenticationToken() {
    ConfigurationService service = createService(null);
    Duration duration = StepVerifier
        .create(
            service.entries(new FetchRequest(new Object()  , "myrepo", "mykey")))
        .expectSubscription()
        .expectError(InvalidAuthenticationToken.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void entries_null_tenant_should_fail_with_InvalidAuthenticationToken() {
    ConfigurationService service = createService(new ProfileBuilder().build());
    Duration duration = StepVerifier
        .create(
            service.entries(new FetchRequest(new Object()  , "myrepo", "mykey")))
        .expectSubscription()
        .expectError(InvalidAuthenticationToken.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void entries_null_claims_should_fail_with_InvalidAuthenticationToken() {
    ConfigurationService service = createService(new ProfileBuilder().tenant("myorg").build());
    Duration duration = StepVerifier
        .create(
            service.entries(new FetchRequest(new Object()  , "myrepo", "mykey")))
        .expectSubscription()
        .expectError(InvalidAuthenticationToken.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void entries_null_role_should_fail_with_InvalidAuthenticationToken() {
    ConfigurationService service = createService(new ProfileBuilder().tenant("myorg")
        .claims(new HashMap<>()).build());
    Duration duration = StepVerifier
        .create(
            service.entries(new FetchRequest(new Object()  , "myrepo", "mykey")))
        .expectSubscription()
        .expectError(InvalidAuthenticationToken.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void entries_should_fail_with_RepositoryNotFoundException() {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", Role.Member);
    ConfigurationService service = createService(new ProfileBuilder().tenant("myorg")
        .claims(claims).build());
    Duration duration = StepVerifier
        .create(
            service.entries(new FetchRequest(new Object()  , "myrepo", "mykey")))
        .expectSubscription()
        .expectError(RepositoryNotFoundException.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void save() {
    ConfigurationService service = createService();
    createRepository(service);
    StepVerifier
        .create(
            service.save(new SaveRequest(new Object(), "myrepo", "mykey",
                mapper.valueToTree(1))))
        .expectSubscription()
        .assertNext(Assertions::assertNotNull)
        .verifyComplete();
    Duration duration = StepVerifier
        .create(
            service.fetch(new FetchRequest(new Object(), "myrepo", "mykey")))
        .expectSubscription()
        .assertNext(result -> assertEquals(String.valueOf(result.value()), "1"))
        .verifyComplete();
    assertNotNull(duration);
  }

  @Test
  void save_null_request_should_fail_withBadRequest() {
    ConfigurationService service = createService(new ProfileBuilder().build());
    Duration duration = StepVerifier
        .create(
            service.save(null))
        .expectSubscription()
        .expectError(BadRequest.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void save_null_repository_should_fail_withBadRequest() {
    ConfigurationService service = createService(new ProfileBuilder().build());
    Duration duration = StepVerifier
        .create(
            service.save(new SaveRequest(new Object(), null, "mykey",
                mapper.valueToTree(1))))
        .expectSubscription()
        .expectError(BadRequest.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void save_null_token_should_fail_withBadRequest() {
    ConfigurationService service = createService(null);
    Duration duration = StepVerifier
        .create(
            service.save(new SaveRequest(null, "myrepo", "mykey",
                mapper.valueToTree(1))))
        .expectSubscription()
        .expectError(BadRequest.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void save_null_key_should_fail_withBadRequest() {
    ConfigurationService service = createService(null);
    Duration duration = StepVerifier
        .create(
            service.save(new SaveRequest(new Object(), "myrepo", null,
                mapper.valueToTree(1))))
        .expectSubscription()
        .expectError(BadRequest.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void save_null_profile_should_fail_with_InvalidAuthenticationToken() {
    ConfigurationService service = createService(null);
    Duration duration = StepVerifier
        .create(
            service.save(new SaveRequest(new Object()  , "myrepo", "mykey",
                mapper.valueToTree(1))))
        .expectSubscription()
        .expectError(InvalidAuthenticationToken.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void save_null_tenant_should_fail_with_InvalidAuthenticationToken() {
    ConfigurationService service = createService(new ProfileBuilder().build());
    Duration duration = StepVerifier
        .create(
            service.save(new SaveRequest(new Object()  , "myrepo", "mykey",
                mapper.valueToTree(1))))
        .expectSubscription()
        .expectError(InvalidAuthenticationToken.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void save_null_claims_should_fail_with_InvalidAuthenticationToken() {
    ConfigurationService service = createService(new ProfileBuilder().tenant("myorg").build());
    Duration duration = StepVerifier
        .create(
            service.save(new SaveRequest(new Object()  , "myrepo", "mykey",
                mapper.valueToTree(1))))
        .expectSubscription()
        .expectError(InvalidAuthenticationToken.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void save_null_role_should_fail_with_InvalidAuthenticationToken() {
    ConfigurationService service = createService(new ProfileBuilder().tenant("myorg")
        .claims(new HashMap<>()).build());
    Duration duration = StepVerifier
        .create(
            service.save(new SaveRequest(new Object()  , "myrepo", "mykey",
                mapper.valueToTree(1))))
        .expectSubscription()
        .expectError(InvalidAuthenticationToken.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void save_should_fail_with_InvalidPermissionsException() {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", Role.Member);
    ConfigurationService service = createService(new ProfileBuilder().tenant("myorg")
        .claims(claims).build());
    Duration duration = StepVerifier
        .create(
            service.save(new SaveRequest(new Object()  , "myrepo", "mykey",
                mapper.valueToTree(1))))
        .expectSubscription()
        .expectError(InvalidPermissionsException.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void save_should_fail_with_RepositoryNotFoundException() {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", Role.Admin);
    ConfigurationService service = createService(new ProfileBuilder().tenant("myorg")
        .claims(claims).build());
    Duration duration = StepVerifier
        .create(
            service.save(new SaveRequest(new Object()  , "myrepo", "mykey",
                mapper.valueToTree(1))))
        .expectSubscription()
        .expectError(RepositoryNotFoundException.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void delete() {
    ConfigurationService service = createService();
    createRepository(service);
    StepVerifier
        .create(
            service.save(new SaveRequest(new Object(), "myrepo", "mykey",
                mapper.valueToTree(1))))
        .expectSubscription()
        .assertNext(Assertions::assertNotNull)
        .verifyComplete();
    Duration duration = StepVerifier
        .create(
            service.delete(new DeleteRequest(new Object(), "myrepo", "mykey")))
        .expectSubscription()
        .assertNext(Assertions::assertNotNull)
        .verifyComplete();
    assertNotNull(duration);
    StepVerifier
        .create(
            service.fetch(new FetchRequest(new Object()  , "myrepo", "mykey")))
        .expectSubscription()
        .expectError(KeyNotFoundException.class)
        .verify();
  }

  @Test
  void delete_should_fail_with_InvalidPermissionsException() {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", Role.Member);
    ConfigurationService service = createService(new ProfileBuilder().tenant("myorg")
        .claims(claims).build());

    Duration duration = StepVerifier
        .create(
            service.delete(new DeleteRequest(new Object()  , "myrepo", "mykey")))
        .expectSubscription()
        .expectError(InvalidPermissionsException.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void delete_null_request_should_fail_withBadRequest() {
    ConfigurationService service = createService();
    Duration duration = StepVerifier
        .create(
            service.delete(null))
        .expectSubscription()
        .expectError(BadRequest.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void delete_null_repository_should_fail_withBadRequest() {
    ConfigurationService service = createService();
    Duration duration = StepVerifier
        .create(
            service.delete(new DeleteRequest(new Object(), null, "mykey")))
        .expectSubscription()
        .expectError(BadRequest.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void delete_null_token_should_fail_withBadRequest() {
    ConfigurationService service = createService();
    Duration duration = StepVerifier
        .create(
            service.delete(new DeleteRequest(null, "myrepo", "mykey")))
        .expectSubscription()
        .expectError(BadRequest.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void delete_null_profile_should_fail_with_InvalidAuthenticationToken() {
    ConfigurationService service = createService(null);
    Duration duration = StepVerifier
        .create(
            service.delete(new DeleteRequest(new Object()  , "myrepo", "mykey")))
        .expectSubscription()
        .expectError(InvalidAuthenticationToken.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void delete_null_tenant_should_fail_with_InvalidAuthenticationToken() {
    ConfigurationService service = createService(new ProfileBuilder().build());
    Duration duration = StepVerifier
        .create(
            service.delete(new DeleteRequest(new Object()  , "myrepo", "mykey")))
        .expectSubscription()
        .expectError(InvalidAuthenticationToken.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void delete_null_claims_should_fail_with_InvalidAuthenticationToken() {
    ConfigurationService service = createService(new ProfileBuilder().tenant("myorg").build());
    Duration duration = StepVerifier
        .create(
            service.delete(new DeleteRequest(new Object()  , "myrepo", "mykey")))
        .expectSubscription()
        .expectError(InvalidAuthenticationToken.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void delete_null_role_should_fail_with_InvalidAuthenticationToken() {
    ConfigurationService service = createService(new ProfileBuilder().tenant("myorg")
        .claims(new HashMap<>()).build());
    Duration duration = StepVerifier
        .create(
            service.delete(new DeleteRequest(new Object()  , "myrepo", "mykey")))
        .expectSubscription()
        .expectError(InvalidAuthenticationToken.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void delete_should_fail_with_RepositoryNotFoundException() {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", Role.Admin);
    ConfigurationService service = createService(new ProfileBuilder().tenant("myorg")
        .claims(claims).build());
    Duration duration = StepVerifier
        .create(
            service.delete(new DeleteRequest(new Object()  , "myrepo", "mykey")))
        .expectSubscription()
        .expectError(RepositoryNotFoundException.class)
        .verify();
    assertNotNull(duration);
  }

  @Test
  void delete_null_key_should_fail_with_BadRequest() {
    ConfigurationService service = createService();
    createRepository(service);
    Duration duration = StepVerifier
        .create(
            service.delete(new DeleteRequest(new Object()  , "myrepo", null)))
        .expectSubscription()
        .expectError(BadRequest.class)
        .verify();
    assertNotNull(duration);
  }


  @Test
  void delete_should_fail_with_KeyNotFoundException() {
    ConfigurationService service = createService();
    createRepository(service);
    Duration duration = StepVerifier
        .create(
            service.delete(new DeleteRequest(new Object()  , "myrepo", "mykey")))
        .expectSubscription()
        .expectError(KeyNotFoundException.class)
        .verify();
    assertNotNull(duration);
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