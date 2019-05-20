package io.scalecube.configuration;

import io.scalecube.account.api.AddOrganizationApiKeyRequest;
import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.DeleteOrganizationRequest;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.configuration.fixtures.ContainersConfigurationServiceFixture;
import io.scalecube.configuration.scenario.CreateRepositoryScenario;
import io.scalecube.test.fixtures.WithFixture;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@WithFixture(value = ContainersConfigurationServiceFixture.class, lifecycle = Lifecycle.PER_METHOD)
public class CreateRepositoryIT extends CreateRepositoryScenario {

  static final String ORGANIZATION_1_EMAIL = "ORGANIZATION_1_EMAIL@testmail.com";
  static final String API_KEY_NAME_1 = "API_KEY_NAME_1";

  static final String ORGANIZATION_2_EMAIL = "ORGANIZATION_2_EMAIL@anyemail.com";
  static final String API_KEY_NAME_2 = "API_KEY_NAME_2";

  static final Map<String, String> TEST_API_CLAIMS = new HashMap<>();

  static {
    TEST_API_CLAIMS.put("role", "Owner");
  }

  protected String org1Id;

  protected String org2Id;

  @BeforeEach
  void before(OrganizationService organizationService) {
    org1Id = organizationService.createOrganization(
        new CreateOrganizationRequest(ORGANIZATION_1, ORGANIZATION_1_EMAIL, AUTH0_TOKEN)).block()
        .id();

    organizationService.addOrganizationApiKey(
        new AddOrganizationApiKeyRequest(AUTH0_TOKEN, org1Id, API_KEY_NAME_1,
            TEST_API_CLAIMS)).block();

    org2Id = organizationService.createOrganization(
        new CreateOrganizationRequest(ORGANIZATION_2, ORGANIZATION_2_EMAIL, AUTH0_TOKEN)).block()
        .id();

    organizationService.addOrganizationApiKey(
        new AddOrganizationApiKeyRequest(AUTH0_TOKEN, org2Id, API_KEY_NAME_2,
            TEST_API_CLAIMS)).block();
  }

  @AfterEach
  void after(OrganizationService organizationService) {
    organizationService.deleteOrganization(new DeleteOrganizationRequest(AUTH0_TOKEN, org1Id))
        .block();
    organizationService.deleteOrganization(new DeleteOrganizationRequest(AUTH0_TOKEN, org2Id))
        .block();
  }
}
