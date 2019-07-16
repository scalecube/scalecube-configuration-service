package io.scalecube.configuration.scenario;

import io.scalecube.account.api.ApiKey;
import io.scalecube.account.api.DeleteOrganizationApiKeyRequest;
import io.scalecube.account.api.DeleteOrganizationRequest;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Role;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.CreateRepositoryRequest;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;
import reactor.test.StepVerifier;

public class CreateRepositoryScenario extends BaseScenario {

  @TestTemplate
  @DisplayName("#1 Scenario: Successful Repository creation applying the \"Owner\" apiKey")
  void createRepositoryByOwner(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String apiKey = createApiKey(organizationService, orgId, Role.Owner).key();

    String repository = RandomStringUtils.randomAlphabetic(5);

    StepVerifier.create(
            configurationService.createRepository(new CreateRepositoryRequest(apiKey, repository)))
        .expectNextCount(1)
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#2  Scenario: Successful Repositories creation with identical names by different organizations applying \"Owner\" apiKey")
  void createIdenticalRepositoryForDifferentOrganizations(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId1 = createOrganization(organizationService).id();
    String apiKey1 = createApiKey(organizationService, orgId1, Role.Owner).key();

    String orgId2 = createOrganization(organizationService).id();
    String apiKey2 = createApiKey(organizationService, orgId2, Role.Owner).key();

    String repository = RandomStringUtils.randomAlphabetic(5);

    StepVerifier.create(
            configurationService.createRepository(new CreateRepositoryRequest(apiKey1, repository)))
        .expectNextCount(1)
        .expectComplete()
        .verify();

    StepVerifier.create(
            configurationService.createRepository(new CreateRepositoryRequest(apiKey2, repository)))
        .expectNextCount(1)
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#3 Scenario: Fail to create the Repository upon access permission is restricted for the \"Admin\" either \"Member\" apiKey")
  void createRepositoryByAdminAndMember(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String adminApiKey = createApiKey(organizationService, orgId, Role.Admin).key();
    String memberApiKey = createApiKey(organizationService, orgId, Role.Member).key();

    String repository = RandomStringUtils.randomAlphabetic(5);

    StepVerifier.create(
            configurationService.createRepository(
                new CreateRepositoryRequest(adminApiKey, repository)))
        .expectErrorMessage(PERMISSION_DENIED)
        .verify();

    StepVerifier.create(
            configurationService.createRepository(
                new CreateRepositoryRequest(memberApiKey, repository)))
        .expectErrorMessage(PERMISSION_DENIED)
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#4 Scenario: Fail to create the Repository with duplicate name for a single Organization applying the \"Owner\" apiKey")
  void createRepositoryWithExistingName(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String apiKey = createApiKey(organizationService, orgId, Role.Owner).key();

    String repository = RandomStringUtils.randomAlphabetic(5);

    configurationService
        .createRepository(new CreateRepositoryRequest(apiKey, repository))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService.createRepository(new CreateRepositoryRequest(apiKey, repository)))
        .expectErrorMessage(String.format(REPOSITORY_ALREADY_EXISTS_FORMATTER, repository))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#5 Scenario: Fail to create the Repository upon the Owner deleted the \"Organization\"")
  void createRepositoryForDeletedOrganization(
      ConfigurationService configurationService, OrganizationService organizationService)
      throws InterruptedException {
    String orgId = createOrganization(organizationService).id();
    String apiKey = createApiKey(organizationService, orgId, Role.Owner).key();

    String repository = RandomStringUtils.randomAlphabetic(5);

    configurationService
        .createRepository(new CreateRepositoryRequest(apiKey, repository))
        .block(TIMEOUT);

    organizationService
        .deleteOrganization(new DeleteOrganizationRequest(AUTH0_TOKEN, orgId))
        .block(TIMEOUT);

    TimeUnit.SECONDS.sleep(KEY_CACHE_TTL + 1);

    StepVerifier.create(
            configurationService.createRepository(new CreateRepositoryRequest(apiKey, repository)))
        .expectErrorMessage(TOKEN_VERIFICATION_FAILED)
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#6 Scenario: Fail to create the Repository upon the \"Owner\" apiKey was deleted from the Organization")
  void createRepositoryUsingDeletedApiKey(
      ConfigurationService configurationService, OrganizationService organizationService)
      throws InterruptedException {
    String orgId = createOrganization(organizationService).id();
    ApiKey ownerApiKey = createApiKey(organizationService, orgId, Role.Owner);

    String repository = RandomStringUtils.randomAlphabetic(5);

    configurationService
        .createRepository(new CreateRepositoryRequest(ownerApiKey.key(), repository))
        .block(TIMEOUT);

    organizationService
        .deleteOrganizationApiKey(
            new DeleteOrganizationApiKeyRequest(AUTH0_TOKEN, orgId, ownerApiKey.name()))
        .block(TIMEOUT);

    TimeUnit.SECONDS.sleep(KEY_CACHE_TTL + 1);

    StepVerifier.create(
            configurationService.createRepository(
                new CreateRepositoryRequest(ownerApiKey.key(), repository)))
        .expectErrorMessage(TOKEN_VERIFICATION_FAILED)
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#7 Scenario: Fail to create the Repository due to invalid apiKey was applied")
  void createRepositoryUsingExpiredApiKey(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String apiKey = getExpiredApiKey(organizationService, orgId, Role.Owner).key();

    String repository = RandomStringUtils.randomAlphabetic(5);

    StepVerifier.create(
            configurationService.createRepository(new CreateRepositoryRequest(apiKey, repository)))
        .expectErrorMessage("Token verification failed")
        .verify();
  }

  @TestTemplate
  @DisplayName("#8 Scenario: Fail to create Repository with empty or undefined name")
  void createRepositoryWithEmptyOrUndefinedName(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String apiKey = createApiKey(organizationService, orgId, Role.Owner).key();

    StepVerifier.create(
        configurationService.createRepository(new CreateRepositoryRequest(apiKey, null)))
        .expectErrorMessage(String.format(PLEASE_SPECIFY_REPO))
        .verify();

    StepVerifier.create(
        configurationService.createRepository(new CreateRepositoryRequest(apiKey, "")))
        .expectErrorMessage(String.format(PLEASE_SPECIFY_REPO))
        .verify();
  }

  @TestTemplate
  @DisplayName("#9 Scenario: Fail to create Repository with empty or undefined apiKey")
  void createRepositoryWithEmptyOrUndefinedApiKey(ConfigurationService configurationService) {
    String repository = RandomStringUtils.randomAlphabetic(5);

    StepVerifier.create(
        configurationService.createRepository(new CreateRepositoryRequest(null, repository)))
        .expectErrorMessage(String.format(PLEASE_SPECIFY_API_KEY))
        .verify();

    StepVerifier.create(
        configurationService.createRepository(new CreateRepositoryRequest("", repository)))
        .expectErrorMessage(String.format(PLEASE_SPECIFY_API_KEY))
        .verify();
  }
}
