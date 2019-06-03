package io.scalecube.configuration.scenario;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.scalecube.account.api.AddOrganizationApiKeyRequest;
import io.scalecube.account.api.ApiKey;
import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.GetOrganizationResponse;
import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;
import reactor.test.StepVerifier;

public abstract class BaseScenario {

  public static final int KEY_CACHE_TTL = 1;
  public static final int KEY_CACHE_REFRESH_INTERVAL = 1;
  public static final int API_KEY_TTL_IN_SECONDS = 3;

  static final Duration TIMEOUT = Duration.ofSeconds(10);
  static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  static final Token AUTH0_TOKEN = new Token("auth0_token");

  @BeforeAll
  static void beforeAll() {
    StepVerifier.setDefaultTimeout(TIMEOUT);
  }

  OrganizationInfo createOrganization(OrganizationService organizationService) {
    return organizationService
        .createOrganization(
            new CreateOrganizationRequest(
                RandomStringUtils.randomAlphabetic(10), "info@scalecube.io", AUTH0_TOKEN))
        .block(TIMEOUT);
  }

  ApiKey getExpiredApiKey(
      OrganizationService organizationService, String organizationId, Role role) {
    ApiKey apiKey = createApiKey(organizationService, organizationId, role);

    try {
      TimeUnit.SECONDS.sleep(API_KEY_TTL_IN_SECONDS + 1);
    } catch (InterruptedException e) {
      throw new RuntimeException("Error on creating expired api key", e);
    }

    return apiKey;
  }

  ApiKey createApiKey(OrganizationService organizationService, String organizationId, Role role) {
    Map<String, String> claims = new HashMap<>();
    claims.put("aud", organizationId);
    claims.put("role", role.name());

    String apiKeyName = RandomStringUtils.randomAlphabetic(5);

    GetOrganizationResponse organization =
        organizationService
            .addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(AUTH0_TOKEN, organizationId, apiKeyName, claims))
            .block(TIMEOUT);

    return Stream.of(
            Optional.ofNullable(organization)
                .orElseThrow(() -> new IllegalStateException("Organization is null"))
                .apiKeys())
        .filter(apiKey -> role.name().equals(apiKey.claims().get("role")))
        .findAny()
        .orElseThrow(() -> new IllegalStateException("ApiKey is null"));
  }

  ObjectNode parse(Object value) {
    try {
      String json;

      if (value instanceof LinkedHashMap) {
        json = OBJECT_MAPPER.writeValueAsString(value);
      } else {
        json = value.toString();
      }

      return OBJECT_MAPPER.readValue(json, ObjectNode.class);
    } catch (IOException e) {
      throw new RuntimeException("Error during parsing value as json", e);
    }
  }
}
