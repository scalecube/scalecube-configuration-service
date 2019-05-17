package io.scalecube.configuration;

import io.scalecube.account.api.AddOrganizationApiKeyRequest;
import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.DeleteOrganizationRequest;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Role;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.DeleteRequest;
import io.scalecube.configuration.fixtures.ContainersConfigurationServiceFixture;
import io.scalecube.configuration.scenario.CreateRepositoryScenario;
import io.scalecube.test.fixtures.WithFixture;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@WithFixture(value = ContainersConfigurationServiceFixture.class, lifecycle = Lifecycle.PER_METHOD)
public class CreateRepositoryITTest extends CreateRepositoryScenario {

  //  static final String TEST_ORG_NAME = "TEST_ORG_NAME";
  static final String ORGANIZATION_1_EMAIL = "ORGANIZATION_1_EMAIL@testmail.com";
  static final String API_KEY_NAME_1 = "API_KEY_NAME_1";

  static final String ORGANIZATION_2_EMAIL = "ORGANIZATION_2_EMAIL@anyemail.com";
  static final String API_KEY_NAME_2 = "API_KEY_NAME_2";

  static final Map<String, String> TEST_API_CLAIMS = new HashMap<>();

  static {
    TEST_API_CLAIMS.put("role", "Owner");
  }

  protected String org1Id;
  protected String org1Token;

  protected String org2Id;
  protected String org2Token;

  @BeforeEach
  void before(OrganizationService organizationService) {
//        org1Id = getOrganization(organizationService, ORGANIZATION_1).id();
    org1Id = organizationService.createOrganization(
        new CreateOrganizationRequest(ORGANIZATION_1, ORGANIZATION_1_EMAIL, AUTH0_TOKEN)).block()
        .id();

    organizationService.addOrganizationApiKey(
        new AddOrganizationApiKeyRequest(AUTH0_TOKEN, org1Id, API_KEY_NAME_1,
            TEST_API_CLAIMS)).block();

//    org1Token = getApiKey(organizationService, org1Id, Role.Owner).key();

//        org2Id = getOrganization(organizationService, ORGANIZATION_2).id();
    org2Id = organizationService.createOrganization(
        new CreateOrganizationRequest(ORGANIZATION_2, ORGANIZATION_2_EMAIL, AUTH0_TOKEN)).block()
        .id();

    organizationService.addOrganizationApiKey(
        new AddOrganizationApiKeyRequest(AUTH0_TOKEN, org2Id, API_KEY_NAME_2,
            TEST_API_CLAIMS)).block();

//    org2Token = getApiKey(organizationService, org2Id, Role.Owner).key();
  }

  @AfterEach
  void after(OrganizationService organizationService) {
//    configurationService.delete(new DeleteRequest(org1Token, "test-repo", API_KEY_NAME_1))
//        .block(TIMEOUT);
//    configurationService.delete(new DeleteRequest(org2Token, "test-repo", API_KEY_NAME_2))
//        .block(TIMEOUT);

    organizationService.deleteOrganization(new DeleteOrganizationRequest(AUTH0_TOKEN, org1Id))
        .block();
    organizationService.deleteOrganization(new DeleteOrganizationRequest(AUTH0_TOKEN, org2Id))
        .block();

//    String orgId1 = organizationService.createOrganization(
//        new CreateOrganizationRequest(ORGANIZATION_1, ORGANIZATION_1_EMAIL, AUTH0_TOKEN)).block()
//        .id();
//
//    organizationService.addOrganizationApiKey(
//        new AddOrganizationApiKeyRequest(AUTH0_TOKEN, orgId1, API_KEY_NAME_1,
//            TEST_API_CLAIMS)).block(TIMEOUT);
//
//    String orgId2 = organizationService.createOrganization(
//        new CreateOrganizationRequest(ORGANIZATION_2, ORGANIZATION_2_EMAIL, AUTH0_TOKEN)).block()
//        .id();
//
//    organizationService.addOrganizationApiKey(
//        new AddOrganizationApiKeyRequest(AUTH0_TOKEN, orgId1, API_KEY_NAME_2,
//            TEST_API_CLAIMS)).block(TIMEOUT);
  }
}
