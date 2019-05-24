package io.scalecube.configuration;

import static io.scalecube.configuration.scenario.BaseScenario.AUTH0_TOKEN;
import static io.scalecube.configuration.scenario.BaseScenario.ORGANIZATION_1;
import static io.scalecube.configuration.scenario.BaseScenario.ORGANIZATION_2;

import io.scalecube.account.api.AddOrganizationApiKeyRequest;
import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.DeleteOrganizationRequest;
import io.scalecube.account.api.OrganizationService;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import reactor.core.publisher.Mono;

public class ITInitBase {

  public static final String ORGANIZATION_1_EMAIL = "ORGANIZATION_1_EMAIL@testmail.com";
  //  static final String API_KEY_NAME_1_OWNER = "API_KEY_NAME_1_OWNER";
  static final String API_KEY_NAME_1_OWNER = "expired";
  static final String API_KEY_NAME_1_ADMIN = "API_KEY_NAME_1_ADMIN";
  static final String API_KEY_NAME_1_MEMBER = "API_KEY_NAME_1_MEMBER";

  static final String ORGANIZATION_2_EMAIL = "ORGANIZATION_2_EMAIL@anyemail.com";
  static final String API_KEY_NAME_2_OWNER = "API_KEY_NAME_2_OWNER";
  static final String API_KEY_NAME_2_ADMIN = "API_KEY_NAME_1_ADMIN";
  static final String API_KEY_NAME_2_MEMBER = "API_KEY_NAME_1_MEMBER";

  static final Map<String, String> TEST_API_OWNER_CLAIMS = new HashMap<>();
  static final Map<String, String> TEST_API_ADMIN_CLAIMS = new HashMap<>();
  static final Map<String, String> TEST_API_MEMBER_CLAIMS = new HashMap<>();

  static {
    TEST_API_OWNER_CLAIMS.put("role", "Owner");
    TEST_API_ADMIN_CLAIMS.put("role", "Admin");
    TEST_API_MEMBER_CLAIMS.put("role", "Member");
  }

  protected String org1Id;

  protected String org2Id;

  public void before(OrganizationService organizationService) throws InterruptedException {
    org1Id = organizationService.createOrganization(
        new CreateOrganizationRequest(ORGANIZATION_1, ORGANIZATION_1_EMAIL, AUTH0_TOKEN)).block()
        .id();

    organizationService.addOrganizationApiKey(
        new AddOrganizationApiKeyRequest(AUTH0_TOKEN, org1Id, API_KEY_NAME_1_OWNER,
            TEST_API_OWNER_CLAIMS)).block();

    organizationService.addOrganizationApiKey(
        new AddOrganizationApiKeyRequest(AUTH0_TOKEN, org1Id, API_KEY_NAME_1_ADMIN,
            TEST_API_ADMIN_CLAIMS)).block();

    organizationService.addOrganizationApiKey(
        new AddOrganizationApiKeyRequest(AUTH0_TOKEN, org1Id, API_KEY_NAME_1_MEMBER,
            TEST_API_MEMBER_CLAIMS)).block();

    org2Id = organizationService.createOrganization(
        new CreateOrganizationRequest(ORGANIZATION_2, ORGANIZATION_2_EMAIL, AUTH0_TOKEN)).block()
        .id();

    organizationService.addOrganizationApiKey(
        new AddOrganizationApiKeyRequest(AUTH0_TOKEN, org2Id, API_KEY_NAME_2_OWNER,
            TEST_API_OWNER_CLAIMS)).block();

    organizationService.addOrganizationApiKey(
        new AddOrganizationApiKeyRequest(AUTH0_TOKEN, org2Id, API_KEY_NAME_2_ADMIN,
            TEST_API_ADMIN_CLAIMS)).block();

    organizationService.addOrganizationApiKey(
        new AddOrganizationApiKeyRequest(AUTH0_TOKEN, org2Id, API_KEY_NAME_2_MEMBER,
            TEST_API_MEMBER_CLAIMS)).block();

    TimeUnit.MILLISECONDS.sleep(1500);
  }

  public void after(OrganizationService organizationService) throws InterruptedException {
    Mono.whenDelayError(
        organizationService.deleteOrganization(new DeleteOrganizationRequest(AUTH0_TOKEN, org1Id)),
        organizationService.deleteOrganization(new DeleteOrganizationRequest(AUTH0_TOKEN, org2Id))
    ).onErrorResume(e -> Mono.empty()).block();

    TimeUnit.MILLISECONDS.sleep(1500);
  }
}
