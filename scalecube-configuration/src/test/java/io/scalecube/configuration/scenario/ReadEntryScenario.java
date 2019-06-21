package io.scalecube.configuration.scenario;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.scalecube.account.api.ApiKey;
import io.scalecube.account.api.DeleteOrganizationApiKeyRequest;
import io.scalecube.account.api.DeleteOrganizationRequest;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Role;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.CreateEntryRequest;
import io.scalecube.configuration.api.CreateRepositoryRequest;
import io.scalecube.configuration.api.ReadEntryRequest;
import io.scalecube.configuration.api.ReadListRequest;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;
import reactor.test.StepVerifier;

public class ReadEntryScenario extends BaseScenario {

  @TestTemplate
  @DisplayName(
      "#18 Successful get of a specific entry from the related Repository applying the all related API keys: \"Owner\", \"Admin\", \"Member\"")
  void readEntry(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String ownerToken = createApiKey(organizationService, orgId, Role.Owner).key();
    String adminToken = createApiKey(organizationService, orgId, Role.Admin).key();
    String memberToken = createApiKey(organizationService, orgId, Role.Member).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey1 = "KEY-FOR-PRECIOUS-METAL-123";
    String entryKey2 = "KEY-FOR-CURRENCY-999";
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
            .put("instrumentId", "JPY")
            .put("name", "Yen")
            .put("DecimalPrecision", 2)
            .put("Rounding", "down");

    configurationService
        .createRepository(new CreateRepositoryRequest(ownerToken, repoName))
        .then(
            configurationService.createEntry(
                new CreateEntryRequest(ownerToken, repoName, entryKey1, entryValue1)))
        .then(
            configurationService.createEntry(
                new CreateEntryRequest(ownerToken, repoName, entryKey2, entryValue2)))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService.readEntry(new ReadEntryRequest(ownerToken, repoName, entryKey1)))
        .assertNext(
            entry -> {
              assertEquals(entryKey1, entry.key(), "Fetched entry key");
              assertEquals(entryValue1, parse(entry.value()), "Fetched entry value");
            })
        .expectComplete()
        .verify();

    StepVerifier.create(
            configurationService.readEntry(new ReadEntryRequest(adminToken, repoName, entryKey1)))
        .assertNext(
            entry -> {
              assertEquals(entryKey1, entry.key(), "Fetched entry key");
              assertEquals(entryValue1, parse(entry.value()), "Fetched entry value");
            })
        .expectComplete()
        .verify();

    StepVerifier.create(
            configurationService.readEntry(new ReadEntryRequest(memberToken, repoName, entryKey1)))
        .assertNext(
            entry -> {
              assertEquals(entryKey1, entry.key(), "Fetched entry key");
              assertEquals(entryValue1, parse(entry.value()), "Fetched entry value");
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#19 Successful get one of the identical entries from the related Repository applying some of the related API keys: \"Owner\", \"Admin\", \"Member\"")
  void readIdenticalEntry(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String ownerToken = createApiKey(organizationService, orgId, Role.Owner).key();
    String memberToken = createApiKey(organizationService, orgId, Role.Member).key();

    String repoName1 = RandomStringUtils.randomAlphabetic(5);
    String repoName2 = "test-repo2";
    String entryKey1 = "KEY-FOR-PRECIOUS-METAL-123";
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
            .put("instrumentId", "JPY")
            .put("name", "Yen")
            .put("DecimalPrecision", 2)
            .put("Rounding", "down");

    configurationService
        .createRepository(new CreateRepositoryRequest(ownerToken, repoName1))
        .then(
            configurationService.createRepository(
                new CreateRepositoryRequest(ownerToken, repoName2)))
        .then(
            configurationService.createEntry(
                new CreateEntryRequest(ownerToken, repoName1, entryKey1, entryValue1)))
        .then(
            configurationService.createEntry(
                new CreateEntryRequest(ownerToken, repoName2, entryKey1, entryValue2)))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService.readEntry(new ReadEntryRequest(memberToken, repoName1, entryKey1)))
        .assertNext(
            entry -> {
              assertEquals(entryKey1, entry.key(), "Fetched entry key");
              assertEquals(entryValue1, parse(entry.value()), "Fetched entry value");
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#20 Fail to get the non-existent entry from the existent Repository applying some of the accessible API keys: \"Owner\", \"Admin\", \"Member\"")
  void readNonExistentEntry(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String ownerToken = createApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey1 = "KEY-FOR-PRECIOUS-METAL-123";
    String entryKey2 = "KEY-FOR-CURRENCY-999";
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
            .put("instrumentId", "JPY")
            .put("name", "Yen")
            .put("DecimalPrecision", 2)
            .put("Rounding", "down");

    configurationService
        .createRepository(new CreateRepositoryRequest(ownerToken, repoName))
        .then(
            configurationService.createEntry(
                new CreateEntryRequest(ownerToken, repoName, entryKey1, entryValue1)))
        .then(
            configurationService.createEntry(
                new CreateEntryRequest(ownerToken, repoName, entryKey2, entryValue2)))
        .block(TIMEOUT);

    String nonExistentKey = "NON_EXISTENT_KEY";

    StepVerifier.create(
            configurationService.readEntry(
                new ReadEntryRequest(ownerToken, repoName, nonExistentKey)))
        .expectErrorMessage(String.format("Key '%s' not found", nonExistentKey))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#21 Fail to get any entry from the non-existent Repository applying some of the accessible API keys: \"Owner\", \"Admin\", \"Member\"")
  void readEntryFromNonExistentRepository(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String token = createApiKey(organizationService, orgId, Role.Admin).key();

    String repoName = "NON_EXISTENT_REPO";

    StepVerifier.create(
            configurationService.readEntry(new ReadEntryRequest(token, repoName, "key")))
        .expectErrorMessage(String.format("Repository '%s-%s' not found", orgId, repoName))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#22 Fail to get the specific entry from the Repository upon the \"apiKey\" is invalid (expired)")
  void readEntryUsingExpiredToken(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String token = getExpiredApiKey(organizationService, orgId, Role.Owner).key();

    StepVerifier.create(configurationService.readList(new ReadListRequest(token, "test-repo")))
        .expectErrorMessage("Token verification failed")
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#23 Fail to get the specific entry from the Repository upon the Owner deleted the Organization with related \"Admin\" API key")
  void readEntryForDeletedOrganization(
      ConfigurationService configurationService, OrganizationService organizationService)
      throws InterruptedException {
    String orgId = createOrganization(organizationService).id();
    String ownerToken = createApiKey(organizationService, orgId, Role.Owner).key();
    String adminToken = createApiKey(organizationService, orgId, Role.Admin).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    configurationService
        .createRepository(new CreateRepositoryRequest(ownerToken, repoName))
        .then(
            configurationService.createEntry(
                new CreateEntryRequest(
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

    organizationService
        .deleteOrganization(new DeleteOrganizationRequest(AUTH0_TOKEN, orgId))
        .block(TIMEOUT);

    TimeUnit.SECONDS.sleep(KEY_CACHE_TTL + 1);

    StepVerifier.create(configurationService.readList(new ReadListRequest(adminToken, repoName)))
        .expectErrorMessage("Token verification failed")
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#24 Fail to get the specific entry from the Repository upon the Owner applied some of the API keys from another Organization")
  void readEntryUsingTokenOfAnotherOrganization(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId1 = createOrganization(organizationService).id();
    String token1 = createApiKey(organizationService, orgId1, Role.Owner).key();

    String orgId2 = createOrganization(organizationService).id();
    String token2 = createApiKey(organizationService, orgId2, Role.Member).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    configurationService
        .createRepository(new CreateRepositoryRequest(token1, repoName))
        .then(
            configurationService.createEntry(
                new CreateEntryRequest(
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

    StepVerifier.create(configurationService.readList(new ReadListRequest(token2, repoName)))
        .expectErrorMessage(String.format("Repository '%s-%s' not found", orgId2, repoName))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#25 Fail to get the specific entry in the Repository upon the Admin \"apiKey\" (API key) was deleted from the Organization")
  void readEntryUsingDeletedToken(
      ConfigurationService configurationService, OrganizationService organizationService)
      throws InterruptedException {
    String orgId = createOrganization(organizationService).id();
    ApiKey ownerToken = createApiKey(organizationService, orgId, Role.Owner);
    ApiKey adminToken = createApiKey(organizationService, orgId, Role.Admin);

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    configurationService
        .createRepository(new CreateRepositoryRequest(ownerToken.key(), repoName))
        .then(
            configurationService.createEntry(
                new CreateEntryRequest(
                    ownerToken.key(),
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
            new DeleteOrganizationApiKeyRequest(AUTH0_TOKEN, orgId, adminToken.name()))
        .block(TIMEOUT);

    TimeUnit.SECONDS.sleep(KEY_CACHE_TTL + 1);

    StepVerifier.create(
            configurationService.readList(new ReadListRequest(adminToken.key(), repoName)))
        .expectErrorMessage("Token verification failed")
        .verify();
  }
}
