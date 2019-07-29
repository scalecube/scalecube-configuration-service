package io.scalecube.configuration.scenario;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.bettercloud.vault.json.Json;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.scalecube.account.api.ApiKey;
import io.scalecube.account.api.DeleteOrganizationApiKeyRequest;
import io.scalecube.account.api.DeleteOrganizationRequest;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Role;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.CreateOrUpdateEntryRequest;
import io.scalecube.configuration.api.CreateRepositoryRequest;
import io.scalecube.configuration.api.ReadEntryRequest;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;
import reactor.test.StepVerifier;

public class UpdateEntryScenario extends BaseScenario {

  @TestTemplate
  @DisplayName(
      "#21 Scenario: Successful updateEntry by one of the identical keys in the different "
          + "Repositories applying the \"Owner\" API key")
  void updateEntry(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String ownerApiKey = createApiKey(organizationService, orgId, Role.Owner).key();
    String adminApiKey = createApiKey(organizationService, orgId, Role.Admin).key();

    String repoName1 = RandomStringUtils.randomAlphabetic(5);
    String repoName2 = RandomStringUtils.randomAlphabetic(5);

    String entryKey1 = "KEY-FOR-PRECIOUS-METAL-123";
    String entryKey2 = "KEY-FOR-CURRENCY-999";

    ObjectNode entryValue11 =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "XAG")
            .put("name", "Silver")
            .put("DecimalPrecision", 4)
            .put("Rounding", "down");
    ObjectNode entryValue21 =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "JPY")
            .put("name", "Yen")
            .put("DecimalPrecision", 2)
            .put("Rounding", "down");
    ObjectNode entryValue12 =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "JPY")
            .put("name", "Silver")
            .put("DecimalPrecision", 4)
            .put("Rounding", "down");
    ObjectNode entryValue22 =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "XAG")
            .put("name", "Yen")
            .put("DecimalPrecision", 2)
            .put("Rounding", "down");

    configurationService
        .createRepository(new CreateRepositoryRequest(ownerApiKey, repoName1))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName1, entryKey1, entryValue11)))
        .then(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName1, entryKey1, entryValue12)))
        .block(TIMEOUT);

    configurationService
        .createRepository(new CreateRepositoryRequest(ownerApiKey, repoName2))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName2, entryKey2, entryValue21)))
        .then(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName2, entryKey2, entryValue22)))
        .block(TIMEOUT);

    StepVerifier.create(
        configurationService.readEntry(new ReadEntryRequest(ownerApiKey, repoName1, entryKey1)))
        .assertNext(
            entry -> {
              assertEquals(entryKey1, entry.key(), "Fetched entry key");
              assertEquals(entryValue12, parse(entry.value()), "Fetched entry value");
            })
        .expectComplete()
        .verify();

    StepVerifier.create(
        configurationService.readEntry(
            new ReadEntryRequest(adminApiKey, repoName1, entryKey1, 1)))
        .assertNext(
            entry -> {
              assertEquals(entryKey1, entry.key(), "Fetched entry key");
              assertEquals(entryValue11, parse(entry.value()), "Fetched entry value");
            })
        .expectComplete()
        .verify();

    StepVerifier.create(
        configurationService.readEntry(
            new ReadEntryRequest(adminApiKey, repoName1, entryKey1, 2)))
        .assertNext(
            entry -> {
              assertEquals(entryKey1, entry.key(), "Fetched entry key");
              assertEquals(entryValue12, parse(entry.value()), "Fetched entry value");
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#21.1 Scenario: Successful updateEntry by one of the identical keys in the different "
          + "Repositories applying the \"Owner\" API key")
  void updateEntryNext(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String ownerApiKey = createApiKey(organizationService, orgId, Role.Owner).key();
    String adminApiKey = createApiKey(organizationService, orgId, Role.Admin).key();
    String memberApiKey = createApiKey(organizationService, orgId, Role.Member).key();

    String repoName1 = RandomStringUtils.randomAlphabetic(5);
    String repoName2 = RandomStringUtils.randomAlphabetic(5);

    String entryKey1 = "KEY-FOR-PRECIOUS-METAL-123";
    String entryKey2 = "KEY-FOR-CURRENCY-999";

    ObjectNode entryValue11 =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "XAG")
            .put("name", "Silver")
            .put("DecimalPrecision", 4)
            .put("Rounding", "down");
    ObjectNode entryValue21 =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "JPY")
            .put("name", "Yen")
            .put("DecimalPrecision", 2)
            .put("Rounding", "down");
    ObjectNode entryValue12 =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "JPY")
            .put("name", "Silver")
            .put("DecimalPrecision", 4)
            .put("Rounding", "down");
    ObjectNode entryValue22 =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "XAG")
            .put("name", "Yen")
            .put("DecimalPrecision", 2)
            .put("Rounding", "down");

    configurationService
        .createRepository(new CreateRepositoryRequest(ownerApiKey, repoName1))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName1, entryKey1, entryValue11)))
        .then(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName1, entryKey1, entryValue12)))
        .block(TIMEOUT);

    configurationService
        .createRepository(new CreateRepositoryRequest(ownerApiKey, repoName2))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName2, entryKey2, entryValue21)))
        .then(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName2, entryKey2, entryValue22)))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService.readEntry(
                new ReadEntryRequest(memberApiKey, repoName2, entryKey2)))
        .assertNext(
            entry -> {
              assertEquals(entryKey2, entry.key(), "Fetched entry key");
              assertEquals(entryValue22, parse(entry.value()), "Fetched entry value");
            })
        .expectComplete()
        .verify();

    StepVerifier.create(
            configurationService.readEntry(
                new ReadEntryRequest(adminApiKey, repoName2, entryKey2, 1)))
        .assertNext(
            entry -> {
              assertEquals(entryKey2, entry.key(), "Fetched entry key");
              assertEquals(entryValue21, parse(entry.value()), "Fetched entry value");
            })
        .expectComplete()
        .verify();

    StepVerifier.create(
            configurationService.readEntry(
                new ReadEntryRequest(adminApiKey, repoName2, entryKey2, 2)))
        .assertNext(
            entry -> {
              assertEquals(entryKey2, entry.key(), "Fetched entry key");
              assertEquals(entryValue22, parse(entry.value()), "Fetched entry value");
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#22 Scenario: Successful updateEntry (no validation for input) enabling to save following "
          + "values:")
  void updateEntryDiffValues(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String ownerApiKey = createApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    ObjectNode entryValue1 = OBJECT_MAPPER.createObjectNode();
    ObjectNode entryValue2 =
        OBJECT_MAPPER.createObjectNode().put("value", JsonArray.create().toString());
    ObjectNode entryValue3 = OBJECT_MAPPER.createObjectNode().put("value", 10);
    ObjectNode entryValue4 = OBJECT_MAPPER.createObjectNode().put("value", Json.NULL.toString());
    ObjectNode entryValue5 =
        OBJECT_MAPPER
            .createObjectNode()
            .put("value", JsonArray.create().add(99).add("some string").toString());
    ObjectNode entryValue6 =
        OBJECT_MAPPER.createObjectNode().put("value", JsonObject.empty().toString());
    ObjectNode entryValue7 =
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
        .then(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName, entryKey, entryValue6)))
        .then(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName, entryKey, entryValue7)))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService.readEntry(new ReadEntryRequest(ownerApiKey, repoName, entryKey)))
        .assertNext(
            entry -> {
              assertEquals(entryKey, entry.key(), "Fetched entry key");
              assertEquals(entryValue7, parse(entry.value()), "Fetched entry value");
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
                new ReadEntryRequest(ownerApiKey, repoName, entryKey, 3)))
        .assertNext(
            entry -> {
              assertEquals(entryKey, entry.key(), "Fetched entry key");
              assertEquals(entryValue3, parse(entry.value()), "Fetched entry value");
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#22.1 Scenario: Successful updateEntry (no validation for input) enabling to save following "
          + "values:")
  void updateEntryDiffValuesNext(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String ownerApiKey = createApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    ObjectNode entryValue1 = OBJECT_MAPPER.createObjectNode();
    ObjectNode entryValue2 =
        OBJECT_MAPPER.createObjectNode().put("value", JsonArray.create().toString());
    ObjectNode entryValue3 = OBJECT_MAPPER.createObjectNode().put("value", 10);
    ObjectNode entryValue4 = OBJECT_MAPPER.createObjectNode().put("value", Json.NULL.toString());
    ObjectNode entryValue5 =
        OBJECT_MAPPER
            .createObjectNode()
            .put("value", JsonArray.create().add(99).add("some string").toString());
    ObjectNode entryValue6 =
        OBJECT_MAPPER.createObjectNode().put("value", JsonObject.empty().toString());
    ObjectNode entryValue7 =
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
        .then(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName, entryKey, entryValue6)))
        .then(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName, entryKey, entryValue7)))
        .block(TIMEOUT);

    StepVerifier.create(
        configurationService.readEntry(
            new ReadEntryRequest(ownerApiKey, repoName, entryKey, 4)))
        .assertNext(
            entry -> {
              assertEquals(entryKey, entry.key(), "Fetched entry key");
              assertEquals(entryValue4, parse(entry.value()), "Fetched entry value");
            })
        .expectComplete()
        .verify();

    StepVerifier.create(
        configurationService.readEntry(
            new ReadEntryRequest(ownerApiKey, repoName, entryKey, 5)))
        .assertNext(
            entry -> {
              assertEquals(entryKey, entry.key(), "Fetched entry key");
              assertEquals(entryValue5, parse(entry.value()), "Fetched entry value");
            })
        .expectComplete()
        .verify();

    StepVerifier.create(
        configurationService.readEntry(
            new ReadEntryRequest(ownerApiKey, repoName, entryKey, 6)))
        .assertNext(
            entry -> {
              assertEquals(entryKey, entry.key(), "Fetched entry key");
              assertEquals(entryValue6, parse(entry.value()), "Fetched entry value");
            })
        .expectComplete()
        .verify();

    StepVerifier.create(
        configurationService.readEntry(
            new ReadEntryRequest(ownerApiKey, repoName, entryKey, 7)))
        .assertNext(
            entry -> {
              assertEquals(entryKey, entry.key(), "Fetched entry key");
              assertEquals(entryValue7, parse(entry.value()), "Fetched entry value");
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#23 Scenario: Fail to updateEntry due to restricted permission upon the \"Member\" API key was applied")
  void updateEntryByAdminAndMember(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String ownerApiKey = createApiKey(organizationService, orgId, Role.Owner).key();
    String memberApiKey = createApiKey(organizationService, orgId, Role.Member).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    ObjectNode entryValue1 = OBJECT_MAPPER.createObjectNode();
    ObjectNode entryValue2 =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "JPY")
            .put("name", "Yen")
            .put("DecimalPrecision", 2)
            .put("Rounding", "down");
    ObjectNode entryValue3 = OBJECT_MAPPER.createObjectNode().put("value", "any");

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
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(memberApiKey, repoName, entryKey, entryValue3)))
        .expectErrorMessage(PERMISSION_DENIED)
        .verify();
  }

  @TestTemplate
  @DisplayName("#24 Scenario: Fail to updateEntry due to specified Repository doesn't exist")
  void updateEntryNotExistsRepo(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String ownerApiKey = createApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String repoNameNotExists = repoName + "_not_exists";
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    ObjectNode entryValue1 = OBJECT_MAPPER.createObjectNode();
    ObjectNode entryValue2 =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "JPY")
            .put("name", "Yen")
            .put("DecimalPrecision", 2)
            .put("Rounding", "down");
    ObjectNode entryValue3 = OBJECT_MAPPER.createObjectNode().put("value", "any");

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
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(
                    ownerApiKey, repoNameNotExists, entryKey, entryValue3)))
        .expectErrorMessage(
            String.format(REPOSITORY_OR_ITS_KEY_NOT_FOUND_FORMATTER, repoNameNotExists, entryKey))
        .verify();
  }

  @TestTemplate
  @DisplayName("#25 Scenario: Fail to updateEntry upon the Owner deleted the \"Organization\"")
  void updateEntryWithDeletedOrganization(
      ConfigurationService configurationService, OrganizationService organizationService)
      throws InterruptedException {
    String orgId = createOrganization(organizationService).id();
    String ownerApiKey = createApiKey(organizationService, orgId, Role.Owner).key();
    String memberApiKey = createApiKey(organizationService, orgId, Role.Member).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String repoNameNotExists = repoName + "_not_exists";
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    ObjectNode entryValue1 = OBJECT_MAPPER.createObjectNode();
    ObjectNode entryValue2 =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "JPY")
            .put("name", "Yen")
            .put("DecimalPrecision", 2)
            .put("Rounding", "down");
    ObjectNode entryValue3 = OBJECT_MAPPER.createObjectNode().put("value", "any");

    configurationService
        .createRepository(new CreateRepositoryRequest(ownerApiKey, repoName))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName, entryKey, entryValue1)))
        .then(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName, entryKey, entryValue2)))
        .block(TIMEOUT);

    organizationService
        .deleteOrganization(new DeleteOrganizationRequest(AUTH0_TOKEN, orgId))
        .block(TIMEOUT);

    TimeUnit.SECONDS.sleep(KEY_CACHE_TTL + 1);

    StepVerifier.create(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(
                    memberApiKey, repoNameNotExists, entryKey, entryValue3)))
        .expectErrorMessage(TOKEN_VERIFICATION_FAILED)
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#26 Scenario: Fail to updateEntry upon the \"Admin\" apiKey was deleted from the Organization")
  void updateEntryWithDeletedAdminApiKey(
      ConfigurationService configurationService, OrganizationService organizationService)
      throws InterruptedException {

    String orgId = createOrganization(organizationService).id();
    String ownerApiKey = createApiKey(organizationService, orgId, Role.Owner).key();
    ApiKey adminApiKey = createApiKey(organizationService, orgId, Role.Admin);

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    ObjectNode entryValue1 = OBJECT_MAPPER.createObjectNode();
    ObjectNode entryValue2 =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "JPY")
            .put("name", "Yen")
            .put("DecimalPrecision", 2)
            .put("Rounding", "down");
    ObjectNode entryValue3 = OBJECT_MAPPER.createObjectNode().put("value", "any");

    configurationService
        .createRepository(new CreateRepositoryRequest(ownerApiKey, repoName))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName, entryKey, entryValue1)))
        .then(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName, entryKey, entryValue2)))
        .block(TIMEOUT);

    organizationService
        .deleteOrganizationApiKey(
            new DeleteOrganizationApiKeyRequest(AUTH0_TOKEN, orgId, adminApiKey.name()))
        .block(TIMEOUT);

    TimeUnit.SECONDS.sleep(KEY_CACHE_TTL + 1);

    StepVerifier.create(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(adminApiKey.key(), repoName, entryKey, entryValue3)))
        .expectErrorMessage(TOKEN_VERIFICATION_FAILED)
        .verify();
  }

  @TestTemplate
  @DisplayName("#27 Scenario: Fail to updateEntry due to invalid apiKey was applied")
  void updateEntryWithInvalidApiKey(
      ConfigurationService configurationService, OrganizationService organizationService) {

    String orgId = createOrganization(organizationService).id();
    ApiKey expiredApiKey = getExpiredApiKey(organizationService, orgId, Role.Admin);

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";
    ObjectNode entryValue =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "JPY")
            .put("name", "Yen")
            .put("DecimalPrecision", 2)
            .put("Rounding", "down");

    StepVerifier.create(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(
                    expiredApiKey.key(), repoName, entryKey, entryValue)))
        .expectErrorMessage(TOKEN_VERIFICATION_FAILED)
        .verify();
  }

  @TestTemplate
  @DisplayName("#28 Scenario: Fail to updateEntry with empty or undefined apiKey")
  void updateEntryWithEmptyOrUndefApiKey(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String ownerApiKey = createApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    ObjectNode entryValue1 = OBJECT_MAPPER.createObjectNode();
    ObjectNode entryValue2 =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "JPY")
            .put("name", "Yen")
            .put("DecimalPrecision", 2)
            .put("Rounding", "down");
    ObjectNode entryValue3 = OBJECT_MAPPER.createObjectNode().put("value", "any");

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
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest("", repoName, entryKey, entryValue3)))
        .expectErrorMessage(PLEASE_SPECIFY_API_KEY)
        .verify();

    StepVerifier.create(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(null, repoName, entryKey, entryValue3)))
        .expectErrorMessage(PLEASE_SPECIFY_API_KEY)
        .verify();
  }

  @TestTemplate
  @DisplayName("#29 Scenario: Fail to updateEntry with empty or undefined Repository name")
  void updateEntryWithEmptyOrUndefRepo(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String ownerApiKey = createApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    ObjectNode entryValue1 = OBJECT_MAPPER.createObjectNode();
    ObjectNode entryValue2 =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "JPY")
            .put("name", "Yen")
            .put("DecimalPrecision", 2)
            .put("Rounding", "down");
    ObjectNode entryValue3 = OBJECT_MAPPER.createObjectNode().put("value", "any");

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
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, "", entryKey, entryValue3)))
        .expectErrorMessage(PLEASE_SPECIFY_REPO)
        .verify();

    StepVerifier.create(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, null, entryKey, entryValue3)))
        .expectErrorMessage(PLEASE_SPECIFY_REPO)
        .verify();
  }

  @TestTemplate
  @DisplayName("#30 Scenario: Fail to updateEntry with empty or undefined Key field")
  void updateEntryWithEmptyOrUndefKey(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String ownerApiKey = createApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    ObjectNode entryValue1 = OBJECT_MAPPER.createObjectNode();
    ObjectNode entryValue2 =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "JPY")
            .put("name", "Yen")
            .put("DecimalPrecision", 2)
            .put("Rounding", "down");
    ObjectNode entryValue3 = OBJECT_MAPPER.createObjectNode().put("value", "any");

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
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName, "", entryValue3)))
        .expectErrorMessage(PLEASE_SPECIFY_KEY)
        .verify();

    StepVerifier.create(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName, null, entryValue3)))
        .expectErrorMessage(PLEASE_SPECIFY_KEY)
        .verify();
  }

  @TestTemplate
  @DisplayName("#31 Scenario: Fail to updateEntry with non-existent Key field")
  void updateEntryWithNonExistsKey(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String ownerApiKey = createApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";
    String entryKeyNotExists = entryKey + "_not_exists";

    ObjectNode entryValue1 = OBJECT_MAPPER.createObjectNode();
    ObjectNode entryValue2 =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "JPY")
            .put("name", "Yen")
            .put("DecimalPrecision", 2)
            .put("Rounding", "down");
    ObjectNode entryValue3 = OBJECT_MAPPER.createObjectNode().put("value", "any");

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
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(
                    ownerApiKey, repoName, entryKeyNotExists, entryValue3)))
        .expectErrorMessage(
            String.format(REPOSITORY_OR_ITS_KEY_NOT_FOUND_FORMATTER, repoName, entryKeyNotExists))
        .verify();
  }
}
