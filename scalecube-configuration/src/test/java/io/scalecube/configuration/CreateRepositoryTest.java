package io.scalecube.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.scalecube.account.api.ApiKey;
import io.scalecube.account.api.DeleteOrganizationApiKeyRequest;
import io.scalecube.account.api.DeleteOrganizationRequest;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Role;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.CreateRepositoryRequest;
import io.scalecube.configuration.api.InvalidAuthenticationToken;
import io.scalecube.configuration.fixtures.InMemoryConfigurationServiceFixture;
import io.scalecube.configuration.repository.exception.RepositoryAlreadyExistsException;
import io.scalecube.test.fixtures.Fixtures;
import io.scalecube.test.fixtures.WithFixture;
import java.security.AccessControlException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import reactor.test.StepVerifier;

@ExtendWith(Fixtures.class)
@WithFixture(value = InMemoryConfigurationServiceFixture.class, lifecycle = Lifecycle.PER_METHOD)
final class CreateRepositoryTest extends BaseTest {

  @TestTemplate
  @DisplayName("#1 Successful Repository creation applying the \"Owner\" API key")
  void createRepositoryByOwner(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = getOrganization(organizationService, ORGANIZATION_1).id();
    String token = getApiKey(organizationService, orgId, Role.Owner).key();

    StepVerifier.create(
            configurationService.createRepository(new CreateRepositoryRequest(token, "test-repo")))
        .expectNextCount(1)
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#1.1 Successful Repositories creation with identical names applying the \"Owner\" API keys from different organizations")
  void createIdenticalRepositoryForDifferentOrganizations(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId1 = getOrganization(organizationService, ORGANIZATION_1).id();
    String token1 = getApiKey(organizationService, orgId1, Role.Owner).key();

    String orgId2 = getOrganization(organizationService, ORGANIZATION_2).id();
    String token2 = getApiKey(organizationService, orgId2, Role.Owner).key();

    String repoName = "test-repo";

    StepVerifier.create(
            configurationService.createRepository(new CreateRepositoryRequest(token1, repoName)))
        .expectNextCount(1)
        .expectComplete()
        .verify();

    StepVerifier.create(
            configurationService.createRepository(new CreateRepositoryRequest(token2, repoName)))
        .expectNextCount(1)
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#2 Fail to create the Repository upon access permission is restricted applying the \"Admin\" either \"Member\" API key")
  void createRepositoryByAdminAndMember(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = getOrganization(organizationService, ORGANIZATION_1).id();
    String adminToken = getApiKey(organizationService, orgId, Role.Admin).key();
    String memberToken = getApiKey(organizationService, orgId, Role.Member).key();

    StepVerifier.create(
            configurationService.createRepository(
                new CreateRepositoryRequest(adminToken, "test-repo")))
        .expectErrorSatisfies(
            e -> {
              assertEquals(AccessControlException.class, e.getClass());
              assertEquals("Permission denied", e.getMessage());
            })
        .verify();

    StepVerifier.create(
            configurationService.createRepository(
                new CreateRepositoryRequest(memberToken, "test-repo")))
        .expectErrorSatisfies(
            e -> {
              assertEquals(AccessControlException.class, e.getClass());
              assertEquals("Permission denied", e.getMessage());
            })
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#3 Fail to create the Repository with the name which already exist (duplicate) applying the \"Owner\" API key")
  void createRepositoryWithExistingName(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = getOrganization(organizationService, ORGANIZATION_1).id();
    String token = getApiKey(organizationService, orgId, Role.Owner).key();
    String repoName = "test-repo";

    configurationService
        .createRepository(new CreateRepositoryRequest(token, repoName))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService.createRepository(new CreateRepositoryRequest(token, repoName)))
        .expectErrorSatisfies(
            e -> {
              assertEquals(RepositoryAlreadyExistsException.class, e.getClass());
              assertEquals(
                  String.format("Repository with name: '%s' already exists", repoName),
                  e.getMessage());
            })
        .verify();
  }

  @TestTemplate
  @DisplayName("#4 Fail to create the Repository upon the \"token\" is invalid (expired)")
  void createRepositoryUsingExpiredToken(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = getOrganization(organizationService, ORGANIZATION_1).id();
    String token = getExpiredApiKey(organizationService, orgId, Role.Owner).key();

    StepVerifier.create(
            configurationService.createRepository(new CreateRepositoryRequest(token, "test-repo")))
        .expectErrorSatisfies(
            e -> {
              assertEquals(InvalidAuthenticationToken.class, e.getClass());
              assertEquals("Token verification failed", e.getMessage());
            })
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#5 Fail to create the Repository upon the Owner deleted the Organization applying the \"Owner\" API key")
  void createRepositoryForDeletedOrganization(
      ConfigurationService configurationService, OrganizationService organizationService)
      throws InterruptedException {
    String orgId = getOrganization(organizationService, ORGANIZATION_1).id();
    String token = getApiKey(organizationService, orgId, Role.Owner).key();

    configurationService
        .createRepository(new CreateRepositoryRequest(token, "test-repo"))
        .block(TIMEOUT);

    organizationService
        .deleteOrganization(new DeleteOrganizationRequest(AUTH0_TOKEN, "ORG-TEST"))
        .block(TIMEOUT);

    TimeUnit.SECONDS.sleep(3);

    StepVerifier.create(
            configurationService.createRepository(new CreateRepositoryRequest(token, "test-repo")))
        .expectErrorSatisfies(
            e -> {
              assertEquals(InvalidAuthenticationToken.class, e.getClass());
              assertEquals("Token verification failed", e.getMessage());
            })
        .verify();
  }

  @Disabled("Feature is not implemented")
  @TestTemplate
  @DisplayName(
      "#6 Fail to create the Repository upon the Owner \"token\" (API key) was deleted from the Organization")
  void createRepositoryUsingDeletedToken(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = getOrganization(organizationService, ORGANIZATION_1).id();
    ApiKey ownerKey = getApiKey(organizationService, orgId, Role.Owner);

    configurationService
        .createRepository(new CreateRepositoryRequest(ownerKey.key(), "test-repo"))
        .block(TIMEOUT);

    organizationService
        .deleteOrganizationApiKey(
            new DeleteOrganizationApiKeyRequest(AUTH0_TOKEN, "ORG-TEST", ownerKey.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService.createRepository(
                new CreateRepositoryRequest(ownerKey.key(), "test-repo")))
        .expectErrorSatisfies(
            e -> {
              assertEquals(InvalidAuthenticationToken.class, e.getClass());
              assertEquals("Token verification failed", e.getMessage());
            })
        .verify();
  }
}
