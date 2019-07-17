package io.scalecube.configuration.scenario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import io.scalecube.configuration.api.ReadEntryHistoryRequest;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;
import reactor.test.StepVerifier;

public class ReadEntryHistoryScenario extends BaseScenario {

  @TestTemplate
  @DisplayName(
      "#63 Scenario: Successful readEntryHistory (all existent versions) from the related Repository applying all API keys roles")
  void readEntryHistory(
      ConfigurationService configurationService, OrganizationService organizationService) {

    String orgId = createOrganization(organizationService).id();
    String ownerToken = createApiKey(organizationService, orgId, Role.Owner).key();
    String memberToken = createApiKey(organizationService, orgId, Role.Member).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    ObjectNode entryValueVersion1 =
        OBJECT_MAPPER.createObjectNode().put("value", JsonArray.create().toString());
    ObjectNode entryValueVersion2 = OBJECT_MAPPER.createObjectNode();
    ObjectNode entryValueVersion3 =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "XAG")
            .put("name", "Silver")
            .put("DecimalPrecision", 2)
            .put("Rounding", "down");
    ObjectNode entryValueVersion4 =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "XPT")
            .put("name", "Platinum")
            .put("DecimalPrecision", 2)
            .put("Rounding", "up");
    ObjectNode entryValueVersion5 =
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
                new CreateOrUpdateEntryRequest(ownerToken, repoName, entryKey, entryValueVersion1)))
        .then(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerToken, repoName, entryKey, entryValueVersion2)))
        .then(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerToken, repoName, entryKey, entryValueVersion3)))
        .then(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerToken, repoName, entryKey, entryValueVersion4)))
        .then(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerToken, repoName, entryKey, entryValueVersion5)))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService.readEntryHistory(
                new ReadEntryHistoryRequest(memberToken, repoName, entryKey)))
        .assertNext(
            entries -> {
              assertEquals(5, entries.size(), "Fetched entries count");

              List<ObjectNode> entriesValues =
                  entries.stream()
                      .map(
                          readEntryHistoryResponse -> {
                            Map<String, Object> entry =
                                (Map<String, Object>) readEntryHistoryResponse.value();
                            ObjectNode entryValue = OBJECT_MAPPER.createObjectNode();
                            entry.forEach(
                                (k, v) -> {
                                  if (v instanceof String) {
                                    entryValue.put(k, (String) v);
                                  } else {
                                    entryValue.put(k, (Integer) v);
                                  }
                                });
                            return entryValue;
                          })
                      .collect(Collectors.toList());

              assertTrue(
                  entriesValues.contains(entryValueVersion1),
                  "Entry for version 1 found in response");
              assertTrue(
                  entriesValues.contains(entryValueVersion2),
                  "Entry for version 2 found in response");
              assertTrue(
                  entriesValues.contains(entryValueVersion3),
                  "Entry for version 3 found in response");
              assertTrue(
                  entriesValues.contains(entryValueVersion4),
                  "Entry for version 4 found in response");
              assertTrue(
                  entriesValues.contains(entryValueVersion5),
                  "Entry for version 5 found in response");
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName("#64 Scenario: Fail to readEntryHistory due to specified Repository doesn't exist")
  void readEntryHistoryWithNotExistsRepo(
      ConfigurationService configurationService, OrganizationService organizationService) {

    String orgId = createOrganization(organizationService).id();
    String ownerToken = createApiKey(organizationService, orgId, Role.Owner).key();
    String memberToken = createApiKey(organizationService, orgId, Role.Member).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String repoNameNotExists = repoName + "_not_exists";
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    ObjectNode entryValueVersion1 =
        OBJECT_MAPPER.createObjectNode().put("value", JsonArray.create().toString());
    ObjectNode entryValueVersion2 =
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
                new CreateOrUpdateEntryRequest(ownerToken, repoName, entryKey, entryValueVersion1)))
        .then(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerToken, repoName, entryKey, entryValueVersion2)))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService.readEntryHistory(
                new ReadEntryHistoryRequest(memberToken, repoNameNotExists, entryKey)))
        .expectErrorMessage(
            String.format(REPOSITORY_OR_ITS_KEY_NOT_FOUND_FORMATTER, repoNameNotExists, entryKey))
        .verify();
  }

  @TestTemplate
  @DisplayName("#65 Scenario: Fail to readEntryHistory upon the Owner deleted the \"Organization\"")
  void readEntryHistoryWithDeletedOrganization(
      ConfigurationService configurationService, OrganizationService organizationService)
      throws InterruptedException {

    String orgId = createOrganization(organizationService).id();
    String ownerToken = createApiKey(organizationService, orgId, Role.Owner).key();
    String memberToken = createApiKey(organizationService, orgId, Role.Member).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    ObjectNode entryValueVersion1 =
        OBJECT_MAPPER.createObjectNode().put("value", JsonArray.create().toString());
    ObjectNode entryValueVersion2 =
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
                new CreateOrUpdateEntryRequest(ownerToken, repoName, entryKey, entryValueVersion1)))
        .then(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerToken, repoName, entryKey, entryValueVersion2)))
        .block(TIMEOUT);

    organizationService
        .deleteOrganization(new DeleteOrganizationRequest(AUTH0_TOKEN, orgId))
        .block(TIMEOUT);

    TimeUnit.SECONDS.sleep(KEY_CACHE_TTL + 1);

    StepVerifier.create(
            configurationService.readEntryHistory(
                new ReadEntryHistoryRequest(memberToken, repoName, entryKey)))
        .expectErrorMessage(TOKEN_VERIFICATION_FAILED)
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#66 Scenario: Fail to readEntryHistory upon the \"Admin\" apiKey was deleted from the Organization")
  void readEntryHistoryWithDeletedAdminApiKey(
      ConfigurationService configurationService, OrganizationService organizationService)
      throws InterruptedException {
    String orgId = createOrganization(organizationService).id();
    String ownerToken = createApiKey(organizationService, orgId, Role.Owner).key();
    ApiKey adminToken = createApiKey(organizationService, orgId, Role.Member);

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    ObjectNode entryValueVersion1 =
        OBJECT_MAPPER.createObjectNode().put("value", JsonArray.create().toString());
    ObjectNode entryValueVersion2 =
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
                new CreateOrUpdateEntryRequest(ownerToken, repoName, entryKey, entryValueVersion1)))
        .then(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerToken, repoName, entryKey, entryValueVersion2)))
        .block(TIMEOUT);

    organizationService
        .deleteOrganizationApiKey(
            new DeleteOrganizationApiKeyRequest(AUTH0_TOKEN, orgId, adminToken.name()))
        .block(TIMEOUT);

    TimeUnit.SECONDS.sleep(KEY_CACHE_TTL + 1);

    StepVerifier.create(
            configurationService.readEntryHistory(
                new ReadEntryHistoryRequest(adminToken, repoName, entryKey)))
        .expectErrorMessage(TOKEN_VERIFICATION_FAILED)
        .verify();
  }

  @TestTemplate
  @DisplayName("#67 Scenario: Fail to readEntryHistory due to invalid apiKey was applied")
  void readEntryHistoryUsingExpiredApiKey(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String expiredToken = getExpiredApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    StepVerifier.create(
            configurationService.readEntryHistory(
                new ReadEntryHistoryRequest(expiredToken, repoName, entryKey)))
        .expectErrorMessage(TOKEN_VERIFICATION_FAILED)
        .verify();
  }

  @TestTemplate
  @DisplayName("#68 Scenario: Fail to readEntryHistory with empty or undefined apiKey")
  void readEntryHistoryWithEmptyOrUndefinedApiKey(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String ownerToken = createApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    ObjectNode entryValueVersion1 =
        OBJECT_MAPPER.createObjectNode().put("value", JsonArray.create().toString());
    ObjectNode entryValueVersion2 =
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
                new CreateOrUpdateEntryRequest(ownerToken, repoName, entryKey, entryValueVersion1)))
        .then(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerToken, repoName, entryKey, entryValueVersion2)))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService.readEntryHistory(
                new ReadEntryHistoryRequest("", repoName, entryKey)))
        .expectErrorMessage(PLEASE_SPECIFY_API_KEY)
        .verify();

    StepVerifier.create(
            configurationService.readEntryHistory(
                new ReadEntryHistoryRequest(null, repoName, entryKey)))
        .expectErrorMessage(PLEASE_SPECIFY_API_KEY)
        .verify();
  }

  @TestTemplate
  @DisplayName("#69 Scenario: Fail to readEntryHistory with empty or undefined Repository name")
  void readEntryHistoryWithEmptyOrUndefinedRepo(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String ownerToken = createApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    ObjectNode entryValueVersion1 =
        OBJECT_MAPPER.createObjectNode().put("value", JsonArray.create().toString());
    ObjectNode entryValueVersion2 =
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
                new CreateOrUpdateEntryRequest(ownerToken, repoName, entryKey, entryValueVersion1)))
        .then(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerToken, repoName, entryKey, entryValueVersion2)))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService.readEntryHistory(
                new ReadEntryHistoryRequest(ownerToken, "", entryKey)))
        .expectErrorMessage(PLEASE_SPECIFY_REPO)
        .verify();

    StepVerifier.create(
            configurationService.readEntryHistory(
                new ReadEntryHistoryRequest(ownerToken, null, entryKey)))
        .expectErrorMessage(PLEASE_SPECIFY_REPO)
        .verify();
  }

  @TestTemplate
  @DisplayName("#70 Scenario: Fail to readEntryHistory with empty or undefined Key field")
  void readEntryHistoryWithEmptyOrUndefinedKey(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String ownerToken = createApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    ObjectNode entryValueVersion1 =
        OBJECT_MAPPER.createObjectNode().put("value", JsonArray.create().toString());
    ObjectNode entryValueVersion2 =
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
                new CreateOrUpdateEntryRequest(ownerToken, repoName, entryKey, entryValueVersion1)))
        .then(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerToken, repoName, entryKey, entryValueVersion2)))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService.readEntryHistory(
                new ReadEntryHistoryRequest(ownerToken, repoName, "")))
        .expectErrorMessage(PLEASE_SPECIFY_KEY)
        .verify();

    StepVerifier.create(
            configurationService.readEntryHistory(
                new ReadEntryHistoryRequest(ownerToken, repoName, null)))
        .expectErrorMessage(PLEASE_SPECIFY_KEY)
        .verify();
  }

  @TestTemplate
  @DisplayName("#71 Scenario: Fail to readEntryHistory with non-existent Key field")
  void readEntryHistoryWithNonExistKey(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String ownerToken = createApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";
    String entryKeyNotExists = entryKey + "_not_exists";

    ObjectNode entryValueVersion1 =
        OBJECT_MAPPER.createObjectNode().put("value", JsonArray.create().toString());
    ObjectNode entryValueVersion2 =
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
                new CreateOrUpdateEntryRequest(ownerToken, repoName, entryKey, entryValueVersion1)))
        .then(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerToken, repoName, entryKey, entryValueVersion2)))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService.readEntryHistory(
                new ReadEntryHistoryRequest(ownerToken, repoName, entryKeyNotExists)))
        .expectErrorMessage(
            String.format(REPOSITORY_OR_ITS_KEY_NOT_FOUND_FORMATTER, repoName, entryKeyNotExists))
        .verify();
  }
}
