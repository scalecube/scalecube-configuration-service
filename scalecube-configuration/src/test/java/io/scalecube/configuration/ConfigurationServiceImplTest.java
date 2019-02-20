package io.scalecube.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.scalecube.configuration.api.Acknowledgment;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.CreateRepositoryRequest;
import io.scalecube.configuration.api.DeleteRequest;
import io.scalecube.configuration.api.FetchRequest;
import io.scalecube.configuration.api.SaveRequest;
import io.scalecube.configuration.authorization.DefaultPermissions;
import io.scalecube.configuration.authorization.Role;
import io.scalecube.configuration.repository.InMemoryDataAccess;
import io.scalecube.configuration.repository.exception.RepositoryAlreadyExistsException;
import io.scalecube.security.acl.DefaultAccessControl;
import io.scalecube.security.api.Profile;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class ConfigurationServiceImplTest {
  private static final String KEY = "key";

  private static final String TOKEN = "token";

  private static final String REPO = "repo";

  private static final ObjectMapper mapper = new ObjectMapper();

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
    StepVerifier.create(service.createRepository(new CreateRepositoryRequest(new Object(), REPO)))
        .expectSubscription()
        .assertNext(r -> assertEquals(Acknowledgment.class, r.getClass()))
        .verifyComplete();
  }

  @Test
  void shouldFailWithRepositoryAlreadyExists() {
    ConfigurationService service = createService();

    StepVerifier.create(service.createRepository(new CreateRepositoryRequest(new Object(), REPO)))
        .expectSubscription()
        .assertNext(r -> assertEquals(Acknowledgment.class, r.getClass()))
        .verifyComplete();

    StepVerifier.create(service.createRepository(new CreateRepositoryRequest(new Object(), REPO)))
        .expectSubscription()
        .expectError(RepositoryAlreadyExistsException.class)
        .verify();
  }

  @Test
  void shouldSaveRequest() {

    ConfigurationService service = createService();

    service.createRepository(new CreateRepositoryRequest(new Object(), REPO)).block();
    StepVerifier.create(service.save(new SaveRequest(TOKEN, REPO, KEY, jsonNode())))
        .expectSubscription()
        .assertNext(r -> assertEquals(Acknowledgment.class, r.getClass()))
        .verifyComplete();
  }

  @Test
  void shouldFetch() {

    ConfigurationService service = createService();

    service.createRepository(new CreateRepositoryRequest(new Object(), REPO)).block();
    service.save(new SaveRequest(TOKEN, REPO, KEY, jsonNode())).block();
    StepVerifier.create(service.fetch(new FetchRequest(TOKEN, REPO, KEY)))
        .expectSubscription()
        .assertNext(r -> assertEquals(r.key(), KEY))
        .verifyComplete();
  }

  @Test
  void shouldFetchAllEntries() {

    ConfigurationService service = createService();

    service.createRepository(new CreateRepositoryRequest(new Object(), REPO)).block();
    service.save(new SaveRequest(TOKEN, REPO, KEY, jsonNode())).block();
    service.save(new SaveRequest(TOKEN, REPO, KEY + "1", jsonNode())).block();
    StepVerifier.create(service.entries(new FetchRequest(TOKEN, REPO)))
        .expectSubscription()
        .assertNext(r -> assertEquals(r.key(), KEY + "1"))
        .assertNext(r -> assertEquals(r.key(), KEY))
        .verifyComplete();
  }

  @Test
  void shouldDelete() {

    ConfigurationService service = createService();

    service.createRepository(new CreateRepositoryRequest(new Object(), REPO)).block();
    service.save(new SaveRequest(TOKEN, REPO, KEY, jsonNode())).block();

    StepVerifier.create(service.delete(new DeleteRequest(TOKEN, REPO, KEY)))
        .expectSubscription()
        .assertNext(r -> assertEquals(Acknowledgment.class, r.getClass()))
        .verifyComplete();
  }

  private static JsonNode jsonNode() {
    try {
      return mapper.readTree("{\"k1\":\"v1\"}");
    } catch (IOException e) {
      return null;
    }
  }
}
