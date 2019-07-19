package io.scalecube.configuration.scenario;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.couchbase.client.java.document.json.JsonArray;
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

public class ReadEntryScenario extends BaseScenario {

  @TestTemplate
  @DisplayName(
      "#42 Scenario: Successful readEntry (latest version) from the related Repository applying all API keys roles")
  void readEntry(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String ownerApiKey = createApiKey(organizationService, orgId, Role.Owner).key();
    String adminApiKey = createApiKey(organizationService, orgId, Role.Admin).key();
    String memberApiKey = createApiKey(organizationService, orgId, Role.Member).key();

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
        .createRepository(new CreateRepositoryRequest(ownerApiKey, repoName))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName, entryKey1, entryValue1)))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName, entryKey2, entryValue2)))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService.readEntry(new ReadEntryRequest(ownerApiKey, repoName, entryKey1)))
        .assertNext(
            entry -> {
              assertEquals(entryKey1, entry.key(), "Fetched entry key");
              assertEquals(entryValue1, parse(entry.value()), "Fetched entry value");
            })
        .expectComplete()
        .verify();

    StepVerifier.create(
            configurationService.readEntry(new ReadEntryRequest(adminApiKey, repoName, entryKey1)))
        .assertNext(
            entry -> {
              assertEquals(entryKey1, entry.key(), "Fetched entry key");
              assertEquals(entryValue1, parse(entry.value()), "Fetched entry value");
            })
        .expectComplete()
        .verify();

    StepVerifier.create(
            configurationService.readEntry(new ReadEntryRequest(memberApiKey, repoName, entryKey1)))
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
    String ownerApiKey = createApiKey(organizationService, orgId, Role.Owner).key();
    String adminApiKey = createApiKey(organizationService, orgId, Role.Admin).key();
    String memberApiKey = createApiKey(organizationService, orgId, Role.Member).key();

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
        .createRepository(new CreateRepositoryRequest(ownerApiKey, repoName))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName, entryKey, entryValue1)))
        .then(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName, entryKey, entryValue2)))
        .then(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName, entryKey, entryValue3)))
        .then(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName, entryKey, entryValue4)))
        .then(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName, entryKey, entryValue5)))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService.readEntry(new ReadEntryRequest(ownerApiKey, repoName, entryKey)))
        .assertNext(
            entry -> {
              assertEquals(entryKey, entry.key(), "Fetched entry key");
              assertEquals(entryValue5, parse(entry.value()), "Fetched entry value");
            })
        .expectComplete()
        .verify();

    StepVerifier.create(
            configurationService.readEntry(
                new ReadEntryRequest(ownerApiKey, repoName, entryKey, 1)))
        .assertNext(
            entry -> {
              assertEquals(entryKey, entry.key(), "Fetched entry key");
              assertEquals(entryValue1, parse(entry.value()), "Fetched entry value");
            })
        .expectComplete()
        .verify();

    StepVerifier.create(
            configurationService.readEntry(
                new ReadEntryRequest(ownerApiKey, repoName, entryKey, 2)))
        .assertNext(
            entry -> {
              assertEquals(entryKey, entry.key(), "Fetched entry key");
              assertEquals(entryValue2, parse(entry.value()), "Fetched entry value");
            })
        .expectComplete()
        .verify();

    StepVerifier.create(
            configurationService.readEntry(
                new ReadEntryRequest(adminApiKey, repoName, entryKey, 3)))
        .assertNext(
            entry -> {
              assertEquals(entryKey, entry.key(), "Fetched entry key");
              assertEquals(entryValue3, parse(entry.value()), "Fetched entry value");
            })
        .expectComplete()
        .verify();

    StepVerifier.create(
            configurationService.readEntry(
                new ReadEntryRequest(memberApiKey, repoName, entryKey, 4)))
        .assertNext(
            entry -> {
              assertEquals(entryKey, entry.key(), "Fetched entry key");
              assertEquals(entryValue4, parse(entry.value()), "Fetched entry value");
            })
        .expectComplete()
        .verify();

    StepVerifier.create(
            configurationService.readEntry(
                new ReadEntryRequest(memberApiKey, repoName, entryKey, 5)))
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
    String ownerApiKey = createApiKey(organizationService, orgId, Role.Owner).key();

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
        .createRepository(new CreateRepositoryRequest(ownerApiKey, repoName))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName, entryKey, entryValue1)))
        .then(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName, entryKey, entryValue2)))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService.readEntry(
                new ReadEntryRequest(ownerApiKey, repoName, entryKey, version)))
        .expectErrorMessage(String.format(KEY_VERSION_NOT_FOUND_FORMATTER, entryKey, version))
        .verify();
  }

  @TestTemplate
  @DisplayName("#44.1 (current error - Failed to decode data on message q=/configuration/readList)")
  void readEntryWithNotPositiveIntegerVersion(
      ConfigurationService configurationService, OrganizationService organizationService) {

    String orgId = createOrganization(organizationService).id();
    String ownerApiKey = createApiKey(organizationService, orgId, Role.Owner).key();
    String memberApiKey = createApiKey(organizationService, orgId, Role.Member).key();

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
        .createRepository(new CreateRepositoryRequest(ownerApiKey, repoName))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName, entryKey, entryValue1)))
        .then(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName, entryKey, entryValue2)))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService.readEntry(
                new ReadEntryRequest(memberApiKey, repoName, entryKey, "afafaf")))
        .expectErrorMessage(VERSION_MUST_BE_A_POSITIVE_NUMBER)
        .verify();

    StepVerifier.create(
            configurationService.readEntry(
                new ReadEntryRequest(memberApiKey, repoName, entryKey, 0)))
        .expectErrorMessage(VERSION_MUST_BE_A_POSITIVE_NUMBER)
        .verify();

    StepVerifier.create(
            configurationService.readEntry(
                new ReadEntryRequest(memberApiKey, repoName, entryKey, -1)))
        .expectErrorMessage(VERSION_MUST_BE_A_POSITIVE_NUMBER)
        .verify();
  }

  @TestTemplate
  @DisplayName("#45 Scenario: Fail to readEntry due to specified Repository doesn't exist")
  void readEntryWithNotExistsRepo(
      ConfigurationService configurationService, OrganizationService organizationService) {

    String orgId = createOrganization(organizationService).id();
    String ownerApiKey = createApiKey(organizationService, orgId, Role.Owner).key();

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
        .createRepository(new CreateRepositoryRequest(ownerApiKey, repoName))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName, entryKey, entryValue)))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService.readEntry(
                new ReadEntryRequest(ownerApiKey, repoNameNotExists, entryKey)))
        .expectErrorMessage(
            String.format(REPOSITORY_OR_ITS_KEY_NOT_FOUND_FORMATTER, repoNameNotExists, entryKey))
        .verify();
  }

  @TestTemplate
  @DisplayName("#46 Scenario: Fail to readEntry upon the Owner deleted the \"Organization\"")
  void readEntryWithDeletedOrganization(
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
            configurationService.readEntry(new ReadEntryRequest(apiKey, repoName, entryKey)))
        .expectErrorMessage(TOKEN_VERIFICATION_FAILED)
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#47 Scenario: Fail to readEntry upon the \"Admin\" apiKey was deleted from the Organization")
  void readEntryWithDeletedAdminApiKey(
      ConfigurationService configurationService, OrganizationService organizationService)
      throws InterruptedException {
    String orgId = createOrganization(organizationService).id();
    ApiKey ownerApiKey = createApiKey(organizationService, orgId, Role.Owner);
    ApiKey adminApiKey = createApiKey(organizationService, orgId, Role.Admin);

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    configurationService
        .createRepository(new CreateRepositoryRequest(ownerApiKey.key(), repoName))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(
                    ownerApiKey.key(),
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
            new DeleteOrganizationApiKeyRequest(AUTH0_TOKEN, orgId, adminApiKey.name()))
        .block(TIMEOUT);

    TimeUnit.SECONDS.sleep(KEY_CACHE_TTL + 1);

    StepVerifier.create(
            configurationService.readEntry(
                new ReadEntryRequest(adminApiKey.key(), repoName, entryKey)))
        .expectErrorMessage(TOKEN_VERIFICATION_FAILED)
        .verify();
  }

  @TestTemplate
  @DisplayName("#48 Scenario: Fail to readEntry due to invalid apiKey was applied")
  void readEntryUsingExpiredApiKey(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String apiKey = getExpiredApiKey(organizationService, orgId, Role.Owner).key();

    String repository = RandomStringUtils.randomAlphabetic(5);

    StepVerifier.create(
            configurationService.readEntry(new ReadEntryRequest(apiKey, repository, "key")))
        .expectErrorMessage(TOKEN_VERIFICATION_FAILED)
        .verify();
  }

  @TestTemplate
  @DisplayName("#49 Scenario: Fail to readEntry with empty or undefined apiKey")
  void readEntryWithEmptyOrUndefinedApiKey(
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
            configurationService.readEntry(new ReadEntryRequest("", repoName, entryKey)))
        .expectErrorMessage(PLEASE_SPECIFY_API_KEY)
        .verify();

    StepVerifier.create(
            configurationService.deleteEntry(new DeleteEntryRequest(null, repoName, entryKey)))
        .expectErrorMessage(PLEASE_SPECIFY_API_KEY)
        .verify();
  }

  @TestTemplate
  @DisplayName("#50 Scenario: Fail to readEntry with empty or undefined Repository name")
  void readEntryWithEmptyOrUndefinedRepo(
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

    StepVerifier.create(configurationService.readEntry(new ReadEntryRequest(apiKey, "", entryKey)))
        .expectErrorMessage(PLEASE_SPECIFY_REPO)
        .verify();

    StepVerifier.create(
            configurationService.deleteEntry(new DeleteEntryRequest(apiKey, null, entryKey)))
        .expectErrorMessage(PLEASE_SPECIFY_REPO)
        .verify();
  }

  @TestTemplate
  @DisplayName("#51 Scenario: Fail to readEntry with empty or undefined Key field")
  void readEntryWithEmptyOrUndefinedKey(
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

    StepVerifier.create(configurationService.readEntry(new ReadEntryRequest(apiKey, repoName, "")))
        .expectErrorMessage(PLEASE_SPECIFY_KEY)
        .verify();

    StepVerifier.create(
            configurationService.deleteEntry(new DeleteEntryRequest(apiKey, repoName, null)))
        .expectErrorMessage(PLEASE_SPECIFY_KEY)
        .verify();
  }

  @TestTemplate
  @DisplayName("#52 Scenario: Fail to readEntry with non-existent Key field")
  void readEntryWithNotExistsKey(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String apiKey = createApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";
    String entryKeyNotExists = entryKey + "_not_exists";

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
            configurationService.readEntry(
                new ReadEntryRequest(apiKey, repoName, entryKeyNotExists)))
        .expectErrorMessage(
            String.format(REPOSITORY_OR_ITS_KEY_NOT_FOUND_FORMATTER, repoName, entryKeyNotExists))
        .verify();
  }
}
