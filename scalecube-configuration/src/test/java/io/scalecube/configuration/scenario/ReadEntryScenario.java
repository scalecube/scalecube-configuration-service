package io.scalecube.configuration.scenario;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.couchbase.client.java.document.json.JsonArray;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Role;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.CreateOrUpdateEntryRequest;
import io.scalecube.configuration.api.CreateRepositoryRequest;
import io.scalecube.configuration.api.ReadEntryRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;
import reactor.test.StepVerifier;

public class ReadEntryScenario extends BaseScenario {

  @TestTemplate
  @DisplayName(
      "#42 Scenario: Successful readEntry (latest version) from the related Repository applying all API keys roles")
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
                new CreateOrUpdateEntryRequest(ownerToken, repoName, entryKey1, entryValue1)))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(ownerToken, repoName, entryKey2, entryValue2)))
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
  @DisplayName("#43 Scenario: Successful readEntry (specific version) from the related Repository")
  void readEntrySpecifiedVersion(
      ConfigurationService configurationService, OrganizationService organizationService) {

    String orgId = createOrganization(organizationService).id();
    String ownerToken = createApiKey(organizationService, orgId, Role.Owner).key();
    String adminToken = createApiKey(organizationService, orgId, Role.Admin).key();
    String memberToken = createApiKey(organizationService, orgId, Role.Member).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    ObjectNode entryValue1 =
        OBJECT_MAPPER.createObjectNode().put("value", JsonArray.create().toString());
    ObjectNode entryValue2 = OBJECT_MAPPER.createObjectNode();
    ObjectNode entryValue3 =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "XAG")
            .put("name", "Silver")
            .put("DecimalPrecision", 2)
            .put("Rounding", "down");
    ObjectNode entryValue4 =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "XPT")
            .put("name", "Platinum")
            .put("DecimalPrecision", 2)
            .put("Rounding", "up");
    ObjectNode entryValue5 =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "XAU")
            .put("name", "Gold")
            .put("DecimalPrecision", 8)
            .put("Rounding", "down");

    configurationService
        .createRepository(new CreateRepositoryRequest(ownerToken, repoName))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(ownerToken, repoName, entryKey, entryValue1)))
        .then(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerToken, repoName, entryKey, entryValue2)))
        .then(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerToken, repoName, entryKey, entryValue3)))
        .then(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerToken, repoName, entryKey, entryValue4)))
        .then(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerToken, repoName, entryKey, entryValue5)))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService.readEntry(new ReadEntryRequest(ownerToken, repoName, entryKey)))
        .assertNext(
            entry -> {
              assertEquals(entryKey, entry.key(), "Fetched entry key");
              assertEquals(entryValue5, parse(entry.value()), "Fetched entry value");
            })
        .expectComplete()
        .verify();

    StepVerifier.create(
            configurationService.readEntry(new ReadEntryRequest(ownerToken, repoName, entryKey, 1)))
        .assertNext(
            entry -> {
              assertEquals(entryKey, entry.key(), "Fetched entry key");
              assertEquals(entryValue1, parse(entry.value()), "Fetched entry value");
            })
        .expectComplete()
        .verify();

    StepVerifier.create(
            configurationService.readEntry(new ReadEntryRequest(ownerToken, repoName, entryKey, 2)))
        .assertNext(
            entry -> {
              assertEquals(entryKey, entry.key(), "Fetched entry key");
              assertEquals(entryValue2, parse(entry.value()), "Fetched entry value");
            })
        .expectComplete()
        .verify();

    StepVerifier.create(
            configurationService.readEntry(new ReadEntryRequest(adminToken, repoName, entryKey, 3)))
        .assertNext(
            entry -> {
              assertEquals(entryKey, entry.key(), "Fetched entry key");
              assertEquals(entryValue3, parse(entry.value()), "Fetched entry value");
            })
        .expectComplete()
        .verify();

    StepVerifier.create(
            configurationService.readEntry(
                new ReadEntryRequest(memberToken, repoName, entryKey, 4)))
        .assertNext(
            entry -> {
              assertEquals(entryKey, entry.key(), "Fetched entry key");
              assertEquals(entryValue4, parse(entry.value()), "Fetched entry value");
            })
        .expectComplete()
        .verify();

    StepVerifier.create(
            configurationService.readEntry(
                new ReadEntryRequest(memberToken, repoName, entryKey, 5)))
        .assertNext(
            entry -> {
              assertEquals(entryKey, entry.key(), "Fetched entry key");
              assertEquals(entryValue5, parse(entry.value()), "Fetched entry value");
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName("#44 Scenario: Fail to readEntry due to non-existent version specified")
  void readEntryWithNotExistingVersion(
      ConfigurationService configurationService, OrganizationService organizationService) {

    String orgId = createOrganization(organizationService).id();
    String ownerToken = createApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";
    int version = 99;

    ObjectNode entryValue1 =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "XAG")
            .put("name", "Silver")
            .put("DecimalPrecision", 2)
            .put("Rounding", "down");
    ObjectNode entryValue2 =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "XPT")
            .put("name", "Platinum")
            .put("DecimalPrecision", 2)
            .put("Rounding", "up");

    configurationService
        .createRepository(new CreateRepositoryRequest(ownerToken, repoName))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(ownerToken, repoName, entryKey, entryValue1)))
        .then(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerToken, repoName, entryKey, entryValue2)))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService.readEntry(
                new ReadEntryRequest(ownerToken, repoName, entryKey, version)))
        .expectErrorMessage(String.format(KEY_VERSION_NOT_FOUND_FORMATTER, entryKey, version))
        .verify();
  }

  @TestTemplate
  @DisplayName("#44.1 (current error - Failed to decode data on message q=/configuration/readList)")
  void readEntryWithNotPositiveIntegerVersion(
      ConfigurationService configurationService, OrganizationService organizationService) {

    String orgId = createOrganization(organizationService).id();
    String ownerToken = createApiKey(organizationService, orgId, Role.Owner).key();
    String memberToken = createApiKey(organizationService, orgId, Role.Member).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    ObjectNode entryValue1 =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "XAG")
            .put("name", "Silver")
            .put("DecimalPrecision", 2)
            .put("Rounding", "down");
    ObjectNode entryValue2 =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "XPT")
            .put("name", "Platinum")
            .put("DecimalPrecision", 2)
            .put("Rounding", "up");

    configurationService
        .createRepository(new CreateRepositoryRequest(ownerToken, repoName))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(ownerToken, repoName, entryKey, entryValue1)))
        .then(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerToken, repoName, entryKey, entryValue2)))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService.readEntry(
                new ReadEntryRequest(memberToken, repoName, entryKey, 0)))
        .expectErrorMessage(VERSION_MUST_BE_A_POSITIVE_NUMBER)
        .verify();

    StepVerifier.create(
            configurationService.readEntry(
                new ReadEntryRequest(memberToken, repoName, entryKey, -1)))
        .expectErrorMessage(VERSION_MUST_BE_A_POSITIVE_NUMBER)
        .verify();
  }

  @TestTemplate
  @DisplayName("#45 Scenario: Fail to readEntry due to specified Repository doesn't exist")
  void readEntryWithNotExistsRepo(
      ConfigurationService configurationService, OrganizationService organizationService) {

    String orgId = createOrganization(organizationService).id();
    String ownerToken = createApiKey(organizationService, orgId, Role.Owner).key();
    String memberToken = createApiKey(organizationService, orgId, Role.Member).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String repoNameNotExists = repoName + "_not_exists";
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    ObjectNode entryValue =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "XAG")
            .put("name", "Silver")
            .put("DecimalPrecision", 2)
            .put("Rounding", "down");

    configurationService
        .createRepository(new CreateRepositoryRequest(ownerToken, repoName))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(ownerToken, repoName, entryKey, entryValue)))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService.readEntry(
                new ReadEntryRequest(ownerToken, repoNameNotExists, entryKey)))
        .expectErrorMessage(
            String.format(REPOSITORY_OR_ITS_KEY_NOT_FOUND_FORMATTER, repoNameNotExists, entryKey))
        .verify();
  }

  //  @TestTemplate
  //  @DisplayName(
  //      "#19 Successful get one of the identical entries from the related Repository applying some
  // of the related API keys: \"Owner\", \"Admin\", \"Member\"")
  //  void readIdenticalEntry(
  //      ConfigurationService configurationService, OrganizationService organizationService) {
  //    String orgId = createOrganization(organizationService).id();
  //    String ownerToken = createApiKey(organizationService, orgId, Role.Owner).key();
  //    String memberToken = createApiKey(organizationService, orgId, Role.Member).key();
  //
  //    String repoName1 = RandomStringUtils.randomAlphabetic(5);
  //    String repoName2 = "test-repo2";
  //    String entryKey1 = "KEY-FOR-PRECIOUS-METAL-123";
  //    ObjectNode entryValue1 =
  //        OBJECT_MAPPER
  //            .createObjectNode()
  //            .put("instrumentId", "XAG")
  //            .put("name", "Silver")
  //            .put("DecimalPrecision", 4)
  //            .put("Rounding", "down");
  //    ObjectNode entryValue2 =
  //        OBJECT_MAPPER
  //            .createObjectNode()
  //            .put("instrumentId", "JPY")
  //            .put("name", "Yen")
  //            .put("DecimalPrecision", 2)
  //            .put("Rounding", "down");
  //
  //    configurationService
  //        .createRepository(new CreateRepositoryRequest(ownerToken, repoName1))
  //        .then(
  //            configurationService.createRepository(
  //                new CreateRepositoryRequest(ownerToken, repoName2)))
  //        .then(
  //            configurationService.createEntry(
  //                new CreateOrUpdateEntryRequest(ownerToken, repoName1, entryKey1, entryValue1)))
  //        .then(
  //            configurationService.createEntry(
  //                new CreateOrUpdateEntryRequest(ownerToken, repoName2, entryKey1, entryValue2)))
  //        .block(TIMEOUT);
  //
  //    StepVerifier.create(
  //            configurationService.readEntry(new ReadEntryRequest(memberToken, repoName1,
  // entryKey1)))
  //        .assertNext(
  //            entry -> {
  //              assertEquals(entryKey1, entry.key(), "Fetched entry key");
  //              assertEquals(entryValue1, parse(entry.value()), "Fetched entry value");
  //            })
  //        .expectComplete()
  //        .verify();
  //  }
  //
  //  @TestTemplate
  //  @DisplayName(
  //      "#20 Fail to get the non-existent entry from the existent Repository applying some of the
  // accessible API keys: \"Owner\", \"Admin\", \"Member\"")
  //  void readNonExistentEntry(
  //      ConfigurationService configurationService, OrganizationService organizationService) {
  //    String orgId = createOrganization(organizationService).id();
  //    String ownerToken = createApiKey(organizationService, orgId, Role.Owner).key();
  //
  //    String repoName = RandomStringUtils.randomAlphabetic(5);
  //    String entryKey1 = "KEY-FOR-PRECIOUS-METAL-123";
  //    String entryKey2 = "KEY-FOR-CURRENCY-999";
  //    ObjectNode entryValue1 =
  //        OBJECT_MAPPER
  //            .createObjectNode()
  //            .put("instrumentId", "XAG")
  //            .put("name", "Silver")
  //            .put("DecimalPrecision", 4)
  //            .put("Rounding", "down");
  //    ObjectNode entryValue2 =
  //        OBJECT_MAPPER
  //            .createObjectNode()
  //            .put("instrumentId", "JPY")
  //            .put("name", "Yen")
  //            .put("DecimalPrecision", 2)
  //            .put("Rounding", "down");
  //
  //    configurationService
  //        .createRepository(new CreateRepositoryRequest(ownerToken, repoName))
  //        .then(
  //            configurationService.createEntry(
  //                new CreateOrUpdateEntryRequest(ownerToken, repoName, entryKey1, entryValue1)))
  //        .then(
  //            configurationService.createEntry(
  //                new CreateOrUpdateEntryRequest(ownerToken, repoName, entryKey2, entryValue2)))
  //        .block(TIMEOUT);
  //
  //    String nonExistentKey = "NON_EXISTENT_KEY";
  //
  //    StepVerifier.create(
  //            configurationService.readEntry(
  //                new ReadEntryRequest(ownerToken, repoName, nonExistentKey)))
  //        .expectErrorMessage(String.format("Key '%s' not found", nonExistentKey))
  //        .verify();
  //  }
  //
  //  @TestTemplate
  //  @DisplayName(
  //      "#21 Fail to get any entry from the non-existent Repository applying some of the
  // accessible API keys: \"Owner\", \"Admin\", \"Member\"")
  //  void readEntryFromNonExistentRepository(
  //      ConfigurationService configurationService, OrganizationService organizationService) {
  //    String orgId = createOrganization(organizationService).id();
  //    String token = createApiKey(organizationService, orgId, Role.Admin).key();
  //
  //    String repoName = "NON_EXISTENT_REPO";
  //
  //    StepVerifier.create(
  //            configurationService.readEntry(new ReadEntryRequest(token, repoName, "key")))
  //        .expectErrorMessage(String.format("Repository '%s' not found", repoName))
  //        .verify();
  //  }
  //
  //  @TestTemplate
  //  @DisplayName(
  //      "#22 Fail to get the specific entry from the Repository upon the \"apiKey\" is invalid
  // (expired)")
  //  void readEntryUsingExpiredToken(
  //      ConfigurationService configurationService, OrganizationService organizationService) {
  //    String orgId = createOrganization(organizationService).id();
  //    String token = getExpiredApiKey(organizationService, orgId, Role.Owner).key();
  //
  //    StepVerifier.create(configurationService.readList(new ReadListRequest(token, "test-repo")))
  //        .expectErrorMessage("Token verification failed")
  //        .verify();
  //  }
  //
  //  @TestTemplate
  //  @DisplayName(
  //      "#23 Fail to get the specific entry from the Repository upon the Owner deleted the
  // Organization with related \"Admin\" API key")
  //  void readEntryForDeletedOrganization(
  //      ConfigurationService configurationService, OrganizationService organizationService)
  //      throws InterruptedException {
  //    String orgId = createOrganization(organizationService).id();
  //    String ownerToken = createApiKey(organizationService, orgId, Role.Owner).key();
  //    String adminToken = createApiKey(organizationService, orgId, Role.Admin).key();
  //
  //    String repoName = RandomStringUtils.randomAlphabetic(5);
  //    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";
  //
  //    configurationService
  //        .createRepository(new CreateRepositoryRequest(ownerToken, repoName))
  //        .then(
  //            configurationService.createEntry(
  //                new CreateOrUpdateEntryRequest(
  //                    ownerToken,
  //                    repoName,
  //                    entryKey,
  //                    OBJECT_MAPPER
  //                        .createObjectNode()
  //                        .put("instrumentId", "XAG")
  //                        .put("name", "Silver")
  //                        .put("DecimalPrecision", 4)
  //                        .put("Rounding", "down"))))
  //        .block(TIMEOUT);
  //
  //    organizationService
  //        .deleteOrganization(new DeleteOrganizationRequest(AUTH0_TOKEN, orgId))
  //        .block(TIMEOUT);
  //
  //    TimeUnit.SECONDS.sleep(KEY_CACHE_TTL + 1);
  //
  //    StepVerifier.create(configurationService.readList(new ReadListRequest(adminToken,
  // repoName)))
  //        .expectErrorMessage("Token verification failed")
  //        .verify();
  //  }
  //
  //  @TestTemplate
  //  @DisplayName(
  //      "#24 Fail to get the specific entry from the Repository upon the Owner applied some of the
  // API keys from another Organization")
  //  void readEntryUsingTokenOfAnotherOrganization(
  //      ConfigurationService configurationService, OrganizationService organizationService) {
  //    String orgId1 = createOrganization(organizationService).id();
  //    String token1 = createApiKey(organizationService, orgId1, Role.Owner).key();
  //
  //    String orgId2 = createOrganization(organizationService).id();
  //    String token2 = createApiKey(organizationService, orgId2, Role.Member).key();
  //
  //    String repoName = RandomStringUtils.randomAlphabetic(5);
  //    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";
  //
  //    configurationService
  //        .createRepository(new CreateRepositoryRequest(token1, repoName))
  //        .then(
  //            configurationService.createEntry(
  //                new CreateOrUpdateEntryRequest(
  //                    token1,
  //                    repoName,
  //                    entryKey,
  //                    OBJECT_MAPPER
  //                        .createObjectNode()
  //                        .put("instrumentId", "XAG")
  //                        .put("name", "Silver")
  //                        .put("DecimalPrecision", 4)
  //                        .put("Rounding", "down"))))
  //        .block(TIMEOUT);
  //
  //    StepVerifier.create(configurationService.readList(new ReadListRequest(token2, repoName)))
  //        .expectErrorMessage(String.format("Repository '%s' not found", repoName))
  //        .verify();
  //  }
  //
  //  @TestTemplate
  //  @DisplayName(
  //      "#25 Fail to get the specific entry in the Repository upon the Admin \"apiKey\" (API key)
  // was deleted from the Organization")
  //  void readEntryUsingDeletedToken(
  //      ConfigurationService configurationService, OrganizationService organizationService)
  //      throws InterruptedException {
  //    String orgId = createOrganization(organizationService).id();
  //    ApiKey ownerToken = createApiKey(organizationService, orgId, Role.Owner);
  //    ApiKey adminToken = createApiKey(organizationService, orgId, Role.Admin);
  //
  //    String repoName = RandomStringUtils.randomAlphabetic(5);
  //    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";
  //
  //    configurationService
  //        .createRepository(new CreateRepositoryRequest(ownerToken.key(), repoName))
  //        .then(
  //            configurationService.createEntry(
  //                new CreateOrUpdateEntryRequest(
  //                    ownerToken.key(),
  //                    repoName,
  //                    entryKey,
  //                    OBJECT_MAPPER
  //                        .createObjectNode()
  //                        .put("instrumentId", "XAG")
  //                        .put("name", "Silver")
  //                        .put("DecimalPrecision", 4)
  //                        .put("Rounding", "down"))))
  //        .block(TIMEOUT);
  //
  //    organizationService
  //        .deleteOrganizationApiKey(
  //            new DeleteOrganizationApiKeyRequest(AUTH0_TOKEN, orgId, adminToken.name()))
  //        .block(TIMEOUT);
  //
  //    TimeUnit.SECONDS.sleep(KEY_CACHE_TTL + 1);
  //
  //    StepVerifier.create(
  //            configurationService.readList(new ReadListRequest(adminToken.key(), repoName)))
  //        .expectErrorMessage("Token verification failed")
  //        .verify();
  //  }
}
