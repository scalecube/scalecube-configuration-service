package io.scalecube.configuration.scenario;

import io.scalecube.account.api.ApiKey;
import io.scalecube.account.api.DeleteOrganizationApiKeyRequest;
import io.scalecube.account.api.DeleteOrganizationRequest;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Role;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.CreateOrUpdateEntryRequest;
import io.scalecube.configuration.api.CreateRepositoryRequest;
import io.scalecube.configuration.api.DeleteEntryRequest;
import io.scalecube.configuration.api.ReadEntryRequest;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;
import reactor.test.StepVerifier;

public class DeleteEntryScenario extends BaseScenario {

  @TestTemplate
  @DisplayName(
      "#32 Scenario: Successful delete one of the identical keys (entries) from the related Repository applying some of the managers' API keys")
  void deleteEntry(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String ownerApiKey = createApiKey(organizationService, orgId, Role.Owner).key();
    String adminApiKey = createApiKey(organizationService, orgId, Role.Admin).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey1 = "KEY-FOR-PRECIOUS-METAL-123";
    String entryKey2 = "KEY-FOR-CURRENCY-999";

    configurationService
        .createRepository(new CreateRepositoryRequest(ownerApiKey, repoName))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(
                    ownerApiKey,
                    repoName,
                    entryKey1,
                    OBJECT_MAPPER
                        .createObjectNode()
                        .put("instrumentId", "XAG")
                        .put("name", "Silver")
                        .put("DecimalPrecision", 4)
                        .put("Rounding", "down"))))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(
                    ownerApiKey,
                    repoName,
                    entryKey2,
                    OBJECT_MAPPER
                        .createObjectNode()
                        .put("instrumentId", "JPY")
                        .put("name", "Yen")
                        .put("DecimalPrecision", 2)
                        .put("Rounding", "down"))))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService
                .deleteEntry(new DeleteEntryRequest(ownerApiKey, repoName, entryKey1))
                .then(
                    configurationService.readEntry(
                        new ReadEntryRequest(ownerApiKey, repoName, entryKey1))))
        .expectErrorMessage(
            String.format(REPOSITORY_OR_ITS_KEY_NOT_FOUND_FORMATTER, repoName, entryKey1))
        .verify();

    StepVerifier.create(
            configurationService
                .deleteEntry(new DeleteEntryRequest(adminApiKey, repoName, entryKey2))
                .then(
                    configurationService.readEntry(
                        new ReadEntryRequest(adminApiKey, repoName, entryKey2))))
        .expectErrorMessage(
            String.format(REPOSITORY_OR_ITS_KEY_NOT_FOUND_FORMATTER, repoName, entryKey2))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#33 Scenario: Fail to deleteEntry due to restricted permission upon the \"Member\" API key was applied")
  void deleteEntryByMember(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String ownerApiKey = createApiKey(organizationService, orgId, Role.Owner).key();
    String memberApiKey = createApiKey(organizationService, orgId, Role.Member).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    configurationService
        .createRepository(new CreateRepositoryRequest(ownerApiKey, repoName))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(
                    ownerApiKey,
                    repoName,
                    entryKey,
                    OBJECT_MAPPER
                        .createObjectNode()
                        .put("instrumentId", "XAG")
                        .put("name", "Silver")
                        .put("DecimalPrecision", 4)
                        .put("Rounding", "down"))))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService
                .deleteEntry(new DeleteEntryRequest(memberApiKey, repoName, entryKey))
                .then(
                    configurationService.readEntry(
                        new ReadEntryRequest(memberApiKey, repoName, entryKey))))
        .expectErrorMessage(PERMISSION_DENIED)
        .verify();
  }

  @TestTemplate
  @DisplayName("#34 Scenario: Fail to deleteEntry due to specified Repository doesn't exist")
  void deleteEntryWithRepoNotExists(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String ownerApiKey = createApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String repoNotExists = repoName + "_not_exists";
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    configurationService
        .createRepository(new CreateRepositoryRequest(ownerApiKey, repoName))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(
                    ownerApiKey, repoName, entryKey, OBJECT_MAPPER.createObjectNode())))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService.deleteEntry(
                new DeleteEntryRequest(ownerApiKey, repoNotExists, entryKey)))
        .expectErrorMessage(
            String.format(REPOSITORY_OR_ITS_KEY_NOT_FOUND_FORMATTER, repoNotExists, entryKey))
        .verify();
  }

  @TestTemplate
  @DisplayName("#35 Scenario: Fail to deleteEntry upon the Owner deleted the \"Organization\"")
  void deleteEntryForDeletedOrganization(
      ConfigurationService configurationService, OrganizationService organizationService)
      throws InterruptedException {
    String orgId = createOrganization(organizationService).id();
    String apiKey = createApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    configurationService
        .createRepository(new CreateRepositoryRequest(apiKey, repoName))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(
                    apiKey,
                    repoName,
                    entryKey,
                    OBJECT_MAPPER
                        .createObjectNode()
                        .put("instrumentId", "XAG")
                        .put("name", "Silver")
                        .put("DecimalPrecision", 4)
                        .put("Rounding", "down"))))
        .block(TIMEOUT);

    organizationService
        .deleteOrganization(new DeleteOrganizationRequest(AUTH0_TOKEN, orgId))
        .block(TIMEOUT);

    TimeUnit.SECONDS.sleep(KEY_CACHE_TTL + 1);

    StepVerifier.create(
            configurationService.deleteEntry(new DeleteEntryRequest(apiKey, repoName, entryKey)))
        .expectErrorMessage(TOKEN_VERIFICATION_FAILED)
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#36 Scenario: Fail to deleteEntry upon the \"Admin\" apiKey was deleted from the Organization")
  void deleteEntryWithDeletedAdminApiKey(
      ConfigurationService configurationService, OrganizationService organizationService)
      throws InterruptedException {
    String orgId = createOrganization(organizationService).id();
    ApiKey apiKeyOwner = createApiKey(organizationService, orgId, Role.Owner);
    ApiKey apiKeyAdmin = createApiKey(organizationService, orgId, Role.Admin);

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    configurationService
        .createRepository(new CreateRepositoryRequest(apiKeyOwner.key(), repoName))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(
                    apiKeyOwner.key(),
                    repoName,
                    entryKey,
                    OBJECT_MAPPER
                        .createObjectNode()
                        .put("instrumentId", "XAG")
                        .put("name", "Silver")
                        .put("DecimalPrecision", 4)
                        .put("Rounding", "down"))))
        .block(TIMEOUT);

    organizationService
        .deleteOrganizationApiKey(
            new DeleteOrganizationApiKeyRequest(AUTH0_TOKEN, orgId, apiKeyAdmin.name()))
        .block(TIMEOUT);

    TimeUnit.SECONDS.sleep(KEY_CACHE_TTL + 1);

    StepVerifier.create(
            configurationService.deleteEntry(
                new DeleteEntryRequest(apiKeyAdmin.key(), repoName, entryKey)))
        .expectErrorMessage(TOKEN_VERIFICATION_FAILED)
        .verify();
  }

  @TestTemplate
  @DisplayName("#37 Scenario: Fail to deleteEntry due to invalid apiKey was applied")
  void deleteEntryUsingExpiredApiKey(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String apiKey = getExpiredApiKey(organizationService, orgId, Role.Owner).key();

    String repository = RandomStringUtils.randomAlphabetic(5);

    StepVerifier.create(
            configurationService.deleteEntry(new DeleteEntryRequest(apiKey, repository, "key")))
        .expectErrorMessage(TOKEN_VERIFICATION_FAILED)
        .verify();
  }

  @TestTemplate
  @DisplayName("#38 Scenario: Fail to deleteEntry with empty or undefined apiKey")
  void deleteEntryWithEmptyOrUndefinedApiKey(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String apiKey = createApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    configurationService
        .createRepository(new CreateRepositoryRequest(apiKey, repoName))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(
                    apiKey,
                    repoName,
                    entryKey,
                    OBJECT_MAPPER
                        .createObjectNode()
                        .put("instrumentId", "XAG")
                        .put("name", "Silver")
                        .put("DecimalPrecision", 4)
                        .put("Rounding", "down"))))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService.deleteEntry(new DeleteEntryRequest("", repoName, entryKey)))
        .expectErrorMessage(PLEASE_SPECIFY_API_KEY)
        .verify();

    StepVerifier.create(
            configurationService.deleteEntry(new DeleteEntryRequest(null, repoName, entryKey)))
        .expectErrorMessage(PLEASE_SPECIFY_API_KEY)
        .verify();
  }

  @TestTemplate
  @DisplayName("#39 Scenario: Fail to deleteEntry with empty or undefined Repository name")
  void deleteEntryWithEmptyOrUndefinedRepo(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String apiKey = createApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    configurationService
        .createRepository(new CreateRepositoryRequest(apiKey, repoName))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(
                    apiKey,
                    repoName,
                    entryKey,
                    OBJECT_MAPPER
                        .createObjectNode()
                        .put("instrumentId", "XAG")
                        .put("name", "Silver")
                        .put("DecimalPrecision", 4)
                        .put("Rounding", "down"))))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService.deleteEntry(new DeleteEntryRequest(apiKey, "", entryKey)))
        .expectErrorMessage(PLEASE_SPECIFY_REPO)
        .verify();

    StepVerifier.create(
            configurationService.deleteEntry(new DeleteEntryRequest(apiKey, null, entryKey)))
        .expectErrorMessage(PLEASE_SPECIFY_REPO)
        .verify();
  }

  @TestTemplate
  @DisplayName("#40 Scenario: Fail to deleteEntry with empty or undefined Key field")
  void deleteEntryWithEmptyOrUndefinedKey(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String apiKey = createApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    configurationService
        .createRepository(new CreateRepositoryRequest(apiKey, repoName))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(
                    apiKey,
                    repoName,
                    entryKey,
                    OBJECT_MAPPER
                        .createObjectNode()
                        .put("instrumentId", "XAG")
                        .put("name", "Silver")
                        .put("DecimalPrecision", 4)
                        .put("Rounding", "down"))))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService.deleteEntry(new DeleteEntryRequest(apiKey, repoName, "")))
        .expectErrorMessage(PLEASE_SPECIFY_KEY)
        .verify();

    StepVerifier.create(
            configurationService.deleteEntry(new DeleteEntryRequest(apiKey, repoName, null)))
        .expectErrorMessage(PLEASE_SPECIFY_KEY)
        .verify();
  }

  @TestTemplate
  @DisplayName("#41 Scenario: Fail to deleteEntry with non-existent Key field")
  void deleteEntryWithNonExistKey(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String apiKey = createApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";
    String entryKeyNotExists = "KEY-FOR-PRECIOUS-METAL-123_not_exists";

    configurationService
        .createRepository(new CreateRepositoryRequest(apiKey, repoName))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(
                    apiKey,
                    repoName,
                    entryKey,
                    OBJECT_MAPPER
                        .createObjectNode()
                        .put("instrumentId", "XAG")
                        .put("name", "Silver")
                        .put("DecimalPrecision", 4)
                        .put("Rounding", "down"))))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService.deleteEntry(
                new DeleteEntryRequest(apiKey, repoName, entryKeyNotExists)))
        .expectErrorMessage(
            String.format(REPOSITORY_OR_ITS_KEY_NOT_FOUND_FORMATTER, repoName, entryKeyNotExists))
        .verify();
  }
}
