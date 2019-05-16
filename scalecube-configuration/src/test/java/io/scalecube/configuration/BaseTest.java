package io.scalecube.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.scalecube.account.api.AddOrganizationApiKeyRequest;
import io.scalecube.account.api.ApiKey;
import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.GetMembershipRequest;
import io.scalecube.account.api.GetMembershipResponse;
import io.scalecube.account.api.GetOrganizationRequest;
import io.scalecube.account.api.GetOrganizationResponse;
import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import reactor.test.StepVerifier;

abstract class BaseTest {

  static final Duration TIMEOUT = Duration.ofSeconds(1);
  //  static final Duration TIMEOUT = Duration.ofSeconds(60);
  static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  //  static final Token AUTH0_TOKEN = new Token("auth0_token");
  static final Token AUTH0_TOKEN = new Token(
      "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Ik5UVTNNVUl3TnprM01FSTBNVGREUlRnME5qVTNRalpDTlVZM1FVSTBNRVUzUlVSR1JqVkRNZyJ9.eyJpc3MiOiJodHRwczovL3ltYWx5c2guYXV0aDAuY29tLyIsInN1YiI6IkV4NVRvbG9JZlE3RzdmUERKZGF0WmRqNFBuMkszNkF3QGNsaWVudHMiLCJhdWQiOiJodHRwczovL3ltYWx5c2guYXV0aDAuY29tL2FwaS92Mi8iLCJpYXQiOjE1NTgwMjE5NjIsImV4cCI6MTU2MDYxMzk2MSwiYXpwIjoiRXg1VG9sb0lmUTdHN2ZQREpkYXRaZGo0UG4ySzM2QXciLCJzY29wZSI6InJlYWQ6Y2xpZW50X2dyYW50cyBjcmVhdGU6Y2xpZW50X2dyYW50cyBkZWxldGU6Y2xpZW50X2dyYW50cyB1cGRhdGU6Y2xpZW50X2dyYW50cyByZWFkOnVzZXJzIHVwZGF0ZTp1c2VycyBkZWxldGU6dXNlcnMgY3JlYXRlOnVzZXJzIHJlYWQ6dXNlcnNfYXBwX21ldGFkYXRhIHVwZGF0ZTp1c2Vyc19hcHBfbWV0YWRhdGEgZGVsZXRlOnVzZXJzX2FwcF9tZXRhZGF0YSBjcmVhdGU6dXNlcnNfYXBwX21ldGFkYXRhIGNyZWF0ZTp1c2VyX3RpY2tldHMgcmVhZDpjbGllbnRzIHVwZGF0ZTpjbGllbnRzIGRlbGV0ZTpjbGllbnRzIGNyZWF0ZTpjbGllbnRzIHJlYWQ6Y2xpZW50X2tleXMgdXBkYXRlOmNsaWVudF9rZXlzIGRlbGV0ZTpjbGllbnRfa2V5cyBjcmVhdGU6Y2xpZW50X2tleXMgcmVhZDpjb25uZWN0aW9ucyB1cGRhdGU6Y29ubmVjdGlvbnMgZGVsZXRlOmNvbm5lY3Rpb25zIGNyZWF0ZTpjb25uZWN0aW9ucyByZWFkOnJlc291cmNlX3NlcnZlcnMgdXBkYXRlOnJlc291cmNlX3NlcnZlcnMgZGVsZXRlOnJlc291cmNlX3NlcnZlcnMgY3JlYXRlOnJlc291cmNlX3NlcnZlcnMgcmVhZDpkZXZpY2VfY3JlZGVudGlhbHMgdXBkYXRlOmRldmljZV9jcmVkZW50aWFscyBkZWxldGU6ZGV2aWNlX2NyZWRlbnRpYWxzIGNyZWF0ZTpkZXZpY2VfY3JlZGVudGlhbHMgcmVhZDpydWxlcyB1cGRhdGU6cnVsZXMgZGVsZXRlOnJ1bGVzIGNyZWF0ZTpydWxlcyByZWFkOnJ1bGVzX2NvbmZpZ3MgdXBkYXRlOnJ1bGVzX2NvbmZpZ3MgZGVsZXRlOnJ1bGVzX2NvbmZpZ3MgcmVhZDplbWFpbF9wcm92aWRlciB1cGRhdGU6ZW1haWxfcHJvdmlkZXIgZGVsZXRlOmVtYWlsX3Byb3ZpZGVyIGNyZWF0ZTplbWFpbF9wcm92aWRlciBibGFja2xpc3Q6dG9rZW5zIHJlYWQ6c3RhdHMgcmVhZDp0ZW5hbnRfc2V0dGluZ3MgdXBkYXRlOnRlbmFudF9zZXR0aW5ncyByZWFkOmxvZ3MgcmVhZDpzaGllbGRzIGNyZWF0ZTpzaGllbGRzIGRlbGV0ZTpzaGllbGRzIHVwZGF0ZTp0cmlnZ2VycyByZWFkOnRyaWdnZXJzIHJlYWQ6Z3JhbnRzIGRlbGV0ZTpncmFudHMgcmVhZDpndWFyZGlhbl9mYWN0b3JzIHVwZGF0ZTpndWFyZGlhbl9mYWN0b3JzIHJlYWQ6Z3VhcmRpYW5fZW5yb2xsbWVudHMgZGVsZXRlOmd1YXJkaWFuX2Vucm9sbG1lbnRzIGNyZWF0ZTpndWFyZGlhbl9lbnJvbGxtZW50X3RpY2tldHMgcmVhZDp1c2VyX2lkcF90b2tlbnMgY3JlYXRlOnBhc3N3b3Jkc19jaGVja2luZ19qb2IgZGVsZXRlOnBhc3N3b3Jkc19jaGVja2luZ19qb2IgcmVhZDpjdXN0b21fZG9tYWlucyBkZWxldGU6Y3VzdG9tX2RvbWFpbnMgY3JlYXRlOmN1c3RvbV9kb21haW5zIHJlYWQ6ZW1haWxfdGVtcGxhdGVzIGNyZWF0ZTplbWFpbF90ZW1wbGF0ZXMgdXBkYXRlOmVtYWlsX3RlbXBsYXRlcyByZWFkOm1mYV9wb2xpY2llcyB1cGRhdGU6bWZhX3BvbGljaWVzIHJlYWQ6cm9sZXMgY3JlYXRlOnJvbGVzIGRlbGV0ZTpyb2xlcyB1cGRhdGU6cm9sZXMiLCJndHkiOiJjbGllbnQtY3JlZGVudGlhbHMifQ.E_n8q256afSyYciUz3brBP068DFvWIGn6CCpOIggxrkyygnC8bIel2mbku_5W3HrW3oXu5UVpKQRG-QRGl8dx1gHPGVPN51hHhn3pgWCN1G3jleTRUrG2BLAcC0oOU32cAN1PVNtAmCwV7IrH347BEw8IIOIxlnT_gXjYCrLVbpnwC6NgncmQ7-jA7brOJ89bYbVZQsOfhDe84RTg30dgJ17Xr9OS9SMvlMQI1l-VK2mbmeA0cEHtbhQNnaD0KPQE2u4MpWU2XuTFkkU19vloF0JZyvrxdbFuha0UopPRCA1uYhVfygcuDWePrqFc_-pbNoM98vYShv_UeMn7vduMg");
  static final String ORGANIZATION_1 = "Test Organization 1";
  static final String ORGANIZATION_2 = "Test Organization 2";

  static final String TEST_ORG_NAME = "TEST_ORG_NAME";
  static final String TEST_ORG_EMAIL = "TEST_ORG_EMAIL@anyemail.com";
  static final String TEST_API_KEY_NAME = "TEST_API_KEY_NAME";
  static final Map<String, String> TEST_API_CLAIMS = new HashMap<>();

  static {
    TEST_API_CLAIMS.put("role", "Owner");
  }


  protected String testOrgIdCreated;

  @BeforeAll
  static void beforeAll() {
    StepVerifier.setDefaultTimeout(TIMEOUT);
  }

  @BeforeEach
  void before(OrganizationService organizationService) {
    testOrgIdCreated = organizationService.createOrganization(
        new CreateOrganizationRequest(TEST_ORG_NAME, TEST_ORG_EMAIL, AUTH0_TOKEN)).block()
        .id();

    organizationService.addOrganizationApiKey(
        new AddOrganizationApiKeyRequest(AUTH0_TOKEN, testOrgIdCreated, TEST_API_KEY_NAME,
            TEST_API_CLAIMS)).block(TIMEOUT);
  }

  OrganizationInfo getOrganization(
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

  ApiKey getApiKey(OrganizationService organizationService, String organizationId, Role role) {
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
