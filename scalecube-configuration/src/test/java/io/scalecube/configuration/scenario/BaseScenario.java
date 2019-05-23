package io.scalecube.configuration.scenario;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.scalecube.account.api.ApiKey;
import io.scalecube.account.api.GetMembershipRequest;
import io.scalecube.account.api.GetMembershipResponse;
import io.scalecube.account.api.GetOrganizationRequest;
import io.scalecube.account.api.GetOrganizationResponse;
import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import reactor.test.StepVerifier;

abstract class BaseScenario {

  protected static final Duration TIMEOUT = Duration.ofSeconds(5);
  static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  protected static final Token AUTH0_TOKEN = new Token("AUTH0_TOKEN_MOCK");
  protected static final String ORGANIZATION_1 = "Test_Organization_Name_1";
  protected static final String ORGANIZATION_2 = "Test_Organization_Name_2";

  @BeforeAll
  static void beforeAll() {
    StepVerifier.setDefaultTimeout(TIMEOUT);
  }

  protected OrganizationInfo getOrganization(
      OrganizationService organizationService, String organizationName) {
    GetMembershipResponse membership =
        organizationService
            .getUserOrganizationsMembership(new GetMembershipRequest(AUTH0_TOKEN))
            .block(TIMEOUT);

    return Stream.of(
        Optional.ofNullable(membership)
            .orElseThrow(() -> new IllegalStateException("Membership is null"))
            .organizations())
        .filter(organizationInfo -> organizationName.equals(organizationInfo.name()))
        .findAny()
        .orElseThrow(
            () -> new IllegalStateException("Organization '" + organizationName + "' not found"));
  }

  ApiKey getExpiredApiKey(
      OrganizationService organizationService, String organizationId, Role role) {
    return getApiKey(organizationService, organizationId, role, true);
  }

  protected ApiKey getApiKey(OrganizationService organizationService, String organizationId,
      Role role) {
    return getApiKey(organizationService, organizationId, role, false);
  }

  private ApiKey getApiKey(
      OrganizationService organizationService, String organizationId, Role role, boolean expired) {
    GetOrganizationResponse organization =
        organizationService
            .getOrganization(new GetOrganizationRequest(AUTH0_TOKEN, organizationId))
            .block(TIMEOUT);

    return Stream.of(
        Optional.ofNullable(organization)
            .orElseThrow(() -> new IllegalStateException("Organization is null"))
            .apiKeys())
        .filter(
            apiKey -> {
              if (expired) {
                return apiKey.name().equals("expired");
              }
              return true;
            })
        .filter(apiKey -> role.name().equals(apiKey.claims().get("role")))
        .findAny()
        .orElseThrow(() -> new IllegalStateException("ApiKey is null"));
  }
}
