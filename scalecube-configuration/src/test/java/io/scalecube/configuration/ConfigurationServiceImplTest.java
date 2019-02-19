package io.scalecube.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.scalecube.configuration.api.BadRequest;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.CreateRepositoryRequest;
import io.scalecube.configuration.api.DeleteRequest;
import io.scalecube.configuration.api.FetchRequest;
import io.scalecube.configuration.api.FetchResponse;
import io.scalecube.configuration.api.InvalidAuthenticationToken;
import io.scalecube.configuration.api.InvalidPermissionsException;
import io.scalecube.configuration.api.SaveRequest;
import io.scalecube.configuration.authorization.Permissions;
import io.scalecube.configuration.repository.InMemoryDataAccess;
import io.scalecube.configuration.repository.exception.DuplicateRepositoryException;
import io.scalecube.configuration.repository.exception.KeyNotFoundException;
import io.scalecube.configuration.repository.exception.RepositoryNotFoundException;
import io.scalecube.security.acl.DefaultAccessControl;
import io.scalecube.security.api.Profile;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ConfigurationServiceImplTest {
  private final ObjectMapper mapper = new ObjectMapper();

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
    return ConfigurationServiceImpl.builder()
        .dataAccess(new InMemoryDataAccess())
        .accessControl(
            DefaultAccessControl.builder()
                .authenticator(new DummyAuthenticator(profile))
                .authorizer(
                    Permissions.builder()
                        .grant("configuration/createRepository", Role.Owner.toString())
                        .build())
                .build())
        .tokenVerifier(token -> Mono.justOrEmpty(profile))
        .build();
  }
  
  @Test
  void create_repository_null_request_should_fail_withBadRequest() {
    ConfigurationService service = createService(Profile.builder().build());
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
    ConfigurationService service = createService(Profile.builder().build());
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
    ConfigurationService service = createService(Profile.builder().tenant("myorg").build());
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
    ConfigurationService service = createService(Profile.builder().tenant("myorg")
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
    ConfigurationService service = createService(member);
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

  /**
 *   #MPA-7103 (#1)
 *     Scenario: Successful Repo creation
 *      Given a user have got a valid "token" (API key) with assigned "owner" role
 *      When this user requested to create the "repository" with "specified" name
 *      Then new "repository" should be created and stored in DB
  */
  @Test
  void createRepository() {
    ConfigurationService service = createService();
    Duration duration = createRepository(service);
    assertNotNull(duration);
  }

  

  @Test
  void fetch() {
    ConfigurationService service = createService();
    createRepository(service);

    SaveRequest saveRequest = new SaveRequest(new Object(), "myrepo", "mykey",
        mapper.valueToTree(1));
    FetchRequest fetchRequest = new FetchRequest(new Object(), "myrepo", "mykey");

    Mono<FetchResponse> saveAndFetch = service.save(saveRequest).then(service.fetch(fetchRequest));

    StepVerifier
        .create(saveAndFetch)
        .expectSubscription()
        .assertNext(result -> assertEquals(String.valueOf(result.value()), "1"))
        .verifyComplete();
  }

  @Test
  void fetch_null_request_should_fail_withBadRequest() {
    ConfigurationService service = createService(Profile.builder().build());
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
    ConfigurationService service = createService(Profile.builder().build());
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
    ConfigurationService service = createService(Profile.builder().build());
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
    ConfigurationService service = createService(Profile.builder().build());
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
    ConfigurationService service = createService(Profile.builder().tenant("myorg").build());
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
    ConfigurationService service = createService(Profile.builder().tenant("myorg")
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
    ConfigurationService service = createService(Profile.builder().tenant("myorg")
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

    Object token = new Object();
    String repository = "myrepo";
    String key1 = "mykey";
    JsonNode value1 = mapper.valueToTree(1);
    String key2 = "mykey2";
    JsonNode value2 = mapper.valueToTree(2);

    SaveRequest saveRequest1 = new SaveRequest(token, repository, key1, value1);
    SaveRequest saveRequest2 = new SaveRequest(token, repository, key2, value2);
    FetchRequest fetchRequest = new FetchRequest(token, repository, null);

    StepVerifier
        .create(
            service.save(saveRequest1).then(service.save(saveRequest2))
                .thenMany(service.entries(fetchRequest))
        )
        .expectSubscription()
        .expectNextCount(2)
        .verifyComplete();
  }

  @Test
  void entries_fetch_null_request_should_fail_withBadRequest() {
    ConfigurationService service = createService(Profile.builder().build());
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
    ConfigurationService service = createService(Profile.builder().build());
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
    ConfigurationService service = createService(Profile.builder().build());
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
    ConfigurationService service = createService(Profile.builder().build());
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
    ConfigurationService service = createService(Profile.builder().tenant("myorg").build());
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
    ConfigurationService service = createService(Profile.builder().tenant("myorg")
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
    ConfigurationService service = createService(Profile.builder().tenant("myorg")
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
  void update() {
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
        service.save(new SaveRequest(new Object(), "myrepo", "mykey",
            mapper.valueToTree(42))))
    .expectSubscription()
    .assertNext(Assertions::assertNotNull)
    .verifyComplete();

    Duration duration = StepVerifier
        .create(
            service.fetch(new FetchRequest(new Object(), "myrepo", "mykey")))
        .expectSubscription()
        .assertNext(result -> assertEquals("42", String.valueOf(result.value())))
        .verifyComplete();

    assertNotNull(duration);
  }


  @Test
  void save_null_request_should_fail_withBadRequest() {
    ConfigurationService service = createService(Profile.builder().build());
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
    ConfigurationService service = createService(Profile.builder().build());
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
    ConfigurationService service = createService(Profile.builder().build());
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
    ConfigurationService service = createService(Profile.builder().tenant("myorg").build());
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
    ConfigurationService service = createService(Profile.builder().tenant("myorg")
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
    ConfigurationService service = createService(Profile.builder().tenant("myorg")
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
    ConfigurationService service = createService(admin);
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

    Object token = new Object();
    String repository = "myrepo";
    String key = "mykey";
    JsonNode value = mapper.valueToTree(1);

    SaveRequest saveRequest = new SaveRequest(token, repository, key, value);
    DeleteRequest deleteRequest = new DeleteRequest(token, repository, key);
    FetchRequest fetchRequest = new FetchRequest(token, repository, key);

    StepVerifier
        .create(
            service.save(saveRequest)
                .then(service.delete(deleteRequest).then(service.fetch(fetchRequest))))
        .expectSubscription()
        .expectError(KeyNotFoundException.class)
        .verify();
  }

  @Test
  void delete_should_fail_with_InvalidPermissionsException() {
    ConfigurationService service = createService(member);

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
    ConfigurationService service = createService(Profile.builder().build());
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
    ConfigurationService service = createService(Profile.builder().tenant("myorg").build());
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
    ConfigurationService service = createService(Profile.builder().tenant("myorg")
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
    ConfigurationService service = createService(Profile.builder().tenant("myorg")
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


}
