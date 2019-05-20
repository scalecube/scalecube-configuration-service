package io.scalecube.configuration.scenario;

import com.auth0.client.auth.AuthAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.net.AuthRequest;
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
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import reactor.test.StepVerifier;

abstract class BaseScenario {

  protected static final Duration TIMEOUT = Duration.ofSeconds(1);
  static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  protected static final Token AUTH0_TOKEN = new Token(
      ((Supplier<String>) () -> {
        AuthAPI authAPI = new AuthAPI("itestsoauth.eu.auth0.com",
            "FbUh6PxUVaH1ZSy7Of03gSu7323NpiZF",
            "szOZdb5x6shfSI02VqVPobcIAyTuTL3C-m3kfN8T-a1nj0EHVtjGtYtS6rPq9B78");
        AuthRequest authRequest = authAPI.requestToken("https://itestsoauth.eu.auth0.com/api/v2/");
        TokenHolder holder = null;
        try {
          holder = authRequest.execute();
        } catch (Auth0Exception e) {
          e.printStackTrace();
        }
        return holder.getAccessToken();
      }).get());

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
