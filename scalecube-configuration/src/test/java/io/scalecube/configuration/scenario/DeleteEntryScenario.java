package io.scalecube.configuration.scenario;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.node.ObjectNode;
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
      "#32 Successful delete of the specific entry from the related Repository applying managers' API keys: \"Owner\" and \"Admin\"")
  void deleteEntry(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String ownerToken = createApiKey(organizationService, orgId, Role.Owner).key();
    String adminToken = createApiKey(organizationService, orgId, Role.Admin).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey1 = "KEY-FOR-PRECIOUS-METAL-123";
    String entryKey2 = "KEY-FOR-CURRENCY-999";

    configurationService
        .createRepository(new CreateRepositoryRequest(ownerToken, repoName))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(
                    ownerToken,
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
                    ownerToken,
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
                .deleteEntry(new DeleteEntryRequest(ownerToken, repoName, entryKey1))
                .then(
                    configurationService.readEntry(
                        new ReadEntryRequest(ownerToken, repoName, entryKey1))))
        .expectErrorMessage(String.format("Key '%s' not found", entryKey1))
        .verify();

    StepVerifier.create(
            configurationService
                .deleteEntry(new DeleteEntryRequest(adminToken, repoName, entryKey2))
                .then(
                    configurationService.readEntry(
                        new ReadEntryRequest(adminToken, repoName, entryKey2))))
        .expectErrorMessage(String.format("Key '%s' not found", entryKey2))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#33 Successful delete one of the identical keys (entries) from the related Repository applying some of the managers' API keys")
  void deleteEntryWithIdenticalKey(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String token = createApiKey(organizationService, orgId, Role.Owner).key();

    String repoName1 = RandomStringUtils.randomAlphabetic(5);
    String repoName2 = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";
    ObjectNode entryValue1 =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "XAG")
            .put("name", "Silver")
            .put("DecimalPrecision", 4)
            .put("Rounding", "down");
    ObjectNode entryValue2 =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "XPT")
            .put("name", "Platinum")
            .put("DecimalPrecision", 2)
            .put("Rounding", "up");

    configurationService
        .createRepository(new CreateRepositoryRequest(token, repoName1))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(token, repoName1, entryKey, entryValue1)))
        .block(TIMEOUT);

    configurationService
        .createRepository(new CreateRepositoryRequest(token, repoName2))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(token, repoName2, entryKey, entryValue2)))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService
                .deleteEntry(new DeleteEntryRequest(token, repoName1, entryKey))
                .then(
                    configurationService.readEntry(
                        new ReadEntryRequest(token, repoName1, entryKey))))
        .expectErrorMessage(String.format("Key '%s' not found", entryKey))
        .verify();

    StepVerifier.create(
            configurationService.readEntry(new ReadEntryRequest(token, repoName2, entryKey)))
        .assertNext(
            entry -> {
              assertEquals(entryKey, entry.key(), "Entry key in " + repoName2);
              assertEquals(entryValue2, parse(entry.value()), "Entry value in " + repoName2);
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#34 Fail to delete a specific entry upon the restricted permission due to applying the \"Member\" API key")
  void deleteEntryByMember(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String ownerToken = createApiKey(organizationService, orgId, Role.Owner).key();
    String memberToken = createApiKey(organizationService, orgId, Role.Member).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    configurationService
        .createRepository(new CreateRepositoryRequest(ownerToken, repoName))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(
                    ownerToken,
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
                .deleteEntry(new DeleteEntryRequest(memberToken, repoName, entryKey))
                .then(
                    configurationService.readEntry(
                        new ReadEntryRequest(memberToken, repoName, entryKey))))
        .expectErrorMessage("Permission denied")
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#35 Fail to delete a non-existent entry from the related Repository applying the \"Admin\" API key")
  void deleteNonExistingEntryByAdmin(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String ownerToken = createApiKey(organizationService, orgId, Role.Owner).key();
    String adminToken = createApiKey(organizationService, orgId, Role.Admin).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";
    String nonExistingEntryKey = "NON_EXISTING_KEY";

    configurationService
        .createRepository(new CreateRepositoryRequest(ownerToken, repoName))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(
                    ownerToken,
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
                .deleteEntry(new DeleteEntryRequest(adminToken, repoName, nonExistingEntryKey))
                .then(
                    configurationService.readEntry(
                        new ReadEntryRequest(adminToken, repoName, entryKey))))
        .expectErrorMessage(String.format("Key '%s' not found", nonExistingEntryKey))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#36 Fail to delete specific entry from the Repository upon the \"apiKey\" is invalid (expired)")
  void deleteEntryUsingExpiredToken(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String token = getExpiredApiKey(organizationService, orgId, Role.Owner).key();

    String repository = RandomStringUtils.randomAlphabetic(5);

    StepVerifier.create(
            configurationService.deleteEntry(new DeleteEntryRequest(token, repository, "key")))
        .expectErrorMessage("Token verification failed")
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#37 Fail to delete specific entry from the Repository upon the Owner deleted the Organization with related \"Owner\" API key")
  void deleteEntryForDeletedOrganization(
      ConfigurationService configurationService, OrganizationService organizationService)
      throws InterruptedException {
    String orgId = createOrganization(organizationService).id();
    String token = createApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    configurationService
        .createRepository(new CreateRepositoryRequest(token, repoName))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(
                    token,
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
            configurationService.deleteEntry(new DeleteEntryRequest(token, repoName, entryKey)))
        .expectErrorMessage("Token verification failed")
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#38 Fail to delete specific entry from the Repository upon the Owner applied some of the API keys from another Organization")
  void deleteEntryUsingTokenOfAnotherOrganization(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId1 = createOrganization(organizationService).id();
    String token1 = createApiKey(organizationService, orgId1, Role.Owner).key();

    String orgId2 = createOrganization(organizationService).id();
    String token2 = createApiKey(organizationService, orgId2, Role.Admin).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    configurationService
        .createRepository(new CreateRepositoryRequest(token1, repoName))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(
                    token1,
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
            configurationService.deleteEntry(new DeleteEntryRequest(token2, repoName, entryKey)))
        .expectErrorMessage(String.format("Repository '%s-%s' not found", orgId2, repoName))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#39 Fail to delete specific entry from the Repository upon the Owner \"apiKey\" (API key) was deleted from the Organization")
  void deleteEntryUsingDeletedToken(
      ConfigurationService configurationService, OrganizationService organizationService)
      throws InterruptedException {
    String orgId = createOrganization(organizationService).id();
    ApiKey token = createApiKey(organizationService, orgId, Role.Owner);

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    configurationService
        .createRepository(new CreateRepositoryRequest(token.key(), repoName))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(
                    token.key(),
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
            new DeleteOrganizationApiKeyRequest(AUTH0_TOKEN, orgId, token.name()))
        .block(TIMEOUT);

    TimeUnit.SECONDS.sleep(KEY_CACHE_TTL + 1);

    StepVerifier.create(
            configurationService.deleteEntry(
                new DeleteEntryRequest(token.key(), repoName, entryKey)))
        .expectErrorMessage("Token verification failed")
        .verify();
  }
}
