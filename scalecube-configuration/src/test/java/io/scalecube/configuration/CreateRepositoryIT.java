package io.scalecube.configuration;

import io.scalecube.account.api.AddOrganizationApiKeyRequest;
import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.DeleteOrganizationRequest;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.fixtures.ContainersConfigurationServiceFixture;
import io.scalecube.configuration.scenario.CreateRepositoryScenario;
import io.scalecube.test.fixtures.WithFixture;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import reactor.core.publisher.Mono;

@WithFixture(value = ContainersConfigurationServiceFixture.class, lifecycle = Lifecycle.PER_CLASS)
public class CreateRepositoryIT extends CreateRepositoryScenario {

  static final String ORGANIZATION_1_EMAIL = "ORGANIZATION_1_EMAIL@testmail.com";
  //  static final String API_KEY_NAME_1_OWNER = "API_KEY_NAME_1_OWNER";
  static final String API_KEY_NAME_1_OWNER = "expired";
  static final String API_KEY_NAME_1_ADMIN = "API_KEY_NAME_1_ADMIN";
  static final String API_KEY_NAME_1_MEMBER = "API_KEY_NAME_1_MEMBER";

  static final String ORGANIZATION_2_EMAIL = "ORGANIZATION_2_EMAIL@anyemail.com";
  static final String API_KEY_NAME_2 = "API_KEY_NAME_2";

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

  @BeforeEach
  void before(OrganizationService organizationService) throws InterruptedException {
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
        new AddOrganizationApiKeyRequest(AUTH0_TOKEN, org2Id, API_KEY_NAME_2,
            TEST_API_OWNER_CLAIMS)).block();

    TimeUnit.MILLISECONDS.sleep(1500);
  }

  @AfterEach
  void after(OrganizationService organizationService) throws InterruptedException {
    Mono.whenDelayError(
        organizationService.deleteOrganization(new DeleteOrganizationRequest(AUTH0_TOKEN, org1Id)),
        organizationService.deleteOrganization(new DeleteOrganizationRequest(AUTH0_TOKEN, org2Id))
    ).onErrorResume(e -> Mono.empty()).block();

    TimeUnit.MILLISECONDS.sleep(1500);
  }

  //  @Override
  @Disabled
  protected void createRepositoryUsingExpiredToken(
      ConfigurationService configurationService, OrganizationService organizationService) {
  }
}
