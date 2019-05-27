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
  @DisplayName("#1 Successful Repository creation applying the \"Owner\" API key")
  void createRepositoryByOwner(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String token = createApiKey(organizationService, orgId, Role.Owner).key();

    String repository = RandomStringUtils.randomAlphabetic(5);

    StepVerifier.create(
            configurationService.createRepository(new CreateRepositoryRequest(token, repository)))
        .expectNextCount(1)
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#1.1 Successful Repositories creation with identical names applying the \"Owner\" API keys from different organizations")
  void createIdenticalRepositoryForDifferentOrganizations(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId1 = createOrganization(organizationService).id();
    String token1 = createApiKey(organizationService, orgId1, Role.Owner).key();

    String orgId2 = createOrganization(organizationService).id();
    String token2 = createApiKey(organizationService, orgId2, Role.Owner).key();

    String repository = RandomStringUtils.randomAlphabetic(5);

    StepVerifier.create(
            configurationService.createRepository(new CreateRepositoryRequest(token1, repository)))
        .expectNextCount(1)
        .expectComplete()
        .verify();

    StepVerifier.create(
            configurationService.createRepository(new CreateRepositoryRequest(token2, repository)))
        .expectNextCount(1)
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#2 Fail to create the Repository upon access permission is restricted applying the \"Admin\" either \"Member\" API key")
  void createRepositoryByAdminAndMember(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String adminToken = createApiKey(organizationService, orgId, Role.Admin).key();
    String memberToken = createApiKey(organizationService, orgId, Role.Member).key();

    String repository = RandomStringUtils.randomAlphabetic(5);

    StepVerifier.create(
            configurationService.createRepository(
                new CreateRepositoryRequest(adminToken, repository)))
        .expectErrorMessage("Permission denied")
        .verify();

    StepVerifier.create(
            configurationService.createRepository(
                new CreateRepositoryRequest(memberToken, repository)))
        .expectErrorMessage("Permission denied")
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#3 Fail to create the Repository with the name which already exist (duplicate) applying the \"Owner\" API key")
  void createRepositoryWithExistingName(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String token = createApiKey(organizationService, orgId, Role.Owner).key();

    String repository = RandomStringUtils.randomAlphabetic(5);

    configurationService
        .createRepository(new CreateRepositoryRequest(token, repository))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService.createRepository(new CreateRepositoryRequest(token, repository)))
        .expectErrorMessage(String.format("Repository with name: '%s' already exists", repository))
        .verify();
  }

  @TestTemplate
  @DisplayName("#4 Fail to create the Repository upon the \"token\" is invalid (expired)")
  void createRepositoryUsingExpiredToken(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String token = getExpiredApiKey(organizationService, orgId, Role.Owner).key();

    String repository = RandomStringUtils.randomAlphabetic(5);

    StepVerifier.create(
            configurationService.createRepository(new CreateRepositoryRequest(token, repository)))
        .expectErrorMessage("Token verification failed")
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#5 Fail to create the Repository upon the Owner deleted the Organization applying the \"Owner\" API key")
  void createRepositoryForDeletedOrganization(
      ConfigurationService configurationService, OrganizationService organizationService)
      throws InterruptedException {
    String orgId = createOrganization(organizationService).id();
    String token = createApiKey(organizationService, orgId, Role.Owner).key();

    String repository = RandomStringUtils.randomAlphabetic(5);

    configurationService
        .createRepository(new CreateRepositoryRequest(token, repository))
        .block(TIMEOUT);

    organizationService
        .deleteOrganization(new DeleteOrganizationRequest(AUTH0_TOKEN, orgId))
        .block(TIMEOUT);

    TimeUnit.SECONDS.sleep(3);

    StepVerifier.create(
            configurationService.createRepository(new CreateRepositoryRequest(token, repository)))
        .expectErrorMessage("Token verification failed")
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#6 Fail to create the Repository upon the Owner \"token\" (API key) was deleted from the Organization")
  void createRepositoryUsingDeletedToken(
      ConfigurationService configurationService, OrganizationService organizationService)
      throws InterruptedException {
    String orgId = createOrganization(organizationService).id();
    ApiKey ownerKey = createApiKey(organizationService, orgId, Role.Owner);

    String repository = RandomStringUtils.randomAlphabetic(5);

    configurationService
        .createRepository(new CreateRepositoryRequest(ownerKey.key(), repository))
        .block(TIMEOUT);

    organizationService
        .deleteOrganizationApiKey(
            new DeleteOrganizationApiKeyRequest(AUTH0_TOKEN, orgId, ownerKey.name()))
        .block(TIMEOUT);

    TimeUnit.SECONDS.sleep(3);

    StepVerifier.create(
            configurationService.createRepository(
                new CreateRepositoryRequest(ownerKey.key(), repository)))
        .expectErrorMessage("Token verification failed")
        .verify();
  }
}
