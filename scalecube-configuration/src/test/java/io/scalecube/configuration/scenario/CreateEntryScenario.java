package io.scalecube.configuration.scenario;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;
import reactor.test.StepVerifier;

public class CreateEntryScenario extends BaseScenario {

  @TestTemplate
  @DisplayName("#9 Successful entry creation applying the \"Owner\" API key")
  void createEntry(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String apiKey = createApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";
    ObjectNode entryValue =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "XAG")
            .put("name", "Silver")
            .put("DecimalPrecision", 4)
            .put("Rounding", "down");

    configurationService
        .createRepository(new CreateRepositoryRequest(apiKey, repoName))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService
                .createEntry(new CreateOrUpdateEntryRequest(apiKey, repoName, entryKey, entryValue))
                .then(
                    configurationService.readEntry(
                        new ReadEntryRequest(apiKey, repoName, entryKey))))
        .assertNext(
            entry -> {
              assertEquals(entryKey, entry.key(), "Saved entry key");
              assertEquals(entryValue, parse(entry.value()), "Saved entry value");
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#10 Scenario: Successful creation of identical key-entries for different Organizations' Repositories applying the \"Owner\" and \"Admin\" API keys")
  void createEntryForDifferentRepositories(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String ownerApiKey = createApiKey(organizationService, orgId, Role.Owner).key();
    String adminApiKey = createApiKey(organizationService, orgId, Role.Admin).key();

    String repoName1 = RandomStringUtils.randomAlphabetic(5);
    String repoName2 = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";
    ObjectNode entryValue =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "XAG")
            .put("name", "Silver")
            .put("DecimalPrecision", 4)
            .put("Rounding", "down");

    configurationService
        .createRepository(new CreateRepositoryRequest(ownerApiKey, repoName1))
        .block(TIMEOUT);

    configurationService
        .createRepository(new CreateRepositoryRequest(ownerApiKey, repoName2))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService
                .createEntry(
                    new CreateOrUpdateEntryRequest(adminApiKey, repoName1, entryKey, entryValue))
                .then(
                    configurationService.readEntry(
                        new ReadEntryRequest(adminApiKey, repoName1, entryKey))))
        .assertNext(
            entry -> {
              assertEquals(entryKey, entry.key(), "Saved entry key");
              assertEquals(entryValue, parse(entry.value()), "Saved entry value");
            })
        .expectComplete()
        .verify();

    StepVerifier.create(
            configurationService
                .createEntry(
                    new CreateOrUpdateEntryRequest(adminApiKey, repoName2, entryKey, entryValue))
                .then(
                    configurationService.readEntry(
                        new ReadEntryRequest(adminApiKey, repoName2, entryKey))))
        .assertNext(
            entry -> {
              assertEquals(entryKey, entry.key(), "Saved entry key");
              assertEquals(entryValue, parse(entry.value()), "Saved entry value");
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#11 Scenario: Successful entry creation (no validation for input) enabling to save "
          + "following values: "
          + "- values that reach at least a 1000 chars\n"
          + "  - values which chars are symbols and spaces\n")
  void createEntryWithDiffValues(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String apiKey = createApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    configurationService
        .createRepository(new CreateRepositoryRequest(apiKey, repoName))
        .block(TIMEOUT);

    String entryKeySomeChars = RandomStringUtils.randomAlphabetic(new Random(5).nextInt(25) + 1);
    ObjectNode entryValueSomeChars =
        OBJECT_MAPPER
            .createObjectNode()
            .put(
                "instrumentId", RandomStringUtils.randomAlphabetic(new Random(5).nextInt(1005) + 1))
            .put("name", RandomStringUtils.randomAlphabetic(new Random(5).nextInt(1005) + 1))
            .put("DecimalPrecision", 1005)
            .put("Rounding", "down");
    StepVerifier.create(
            configurationService
                .createEntry(
                    new CreateOrUpdateEntryRequest(
                        apiKey, repoName, entryKeySomeChars, entryValueSomeChars))
                .then(
                    configurationService.readEntry(
                        new ReadEntryRequest(apiKey, repoName, entryKeySomeChars, 1))))
        .assertNext(
            entry -> {
              assertEquals(entryKeySomeChars, entry.key(), "Saved entry key");
              assertEquals(entryValueSomeChars, parse(entry.value()), "Saved entry value");
            })
        .expectComplete()
        .verify();

    String entryKeySomeSymbs = RandomStringUtils.random(new Random(5).nextInt(25) + 1);
    ObjectNode entrySomeSymbs =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", RandomStringUtils.random(new Random(5).nextInt(25) + 1))
            .put("name", RandomStringUtils.random(new Random(5).nextInt(1005) + 1))
            .put("DecimalPrecision", 1005)
            .put("Rounding", "down");
    StepVerifier.create(
            configurationService
                .createEntry(
                    new CreateOrUpdateEntryRequest(
                        apiKey, repoName, entryKeySomeSymbs, entrySomeSymbs))
                .then(
                    configurationService.readEntry(
                        new ReadEntryRequest(apiKey, repoName, entryKeySomeSymbs, 1))))
        .assertNext(
            entry -> {
              assertEquals(entryKeySomeSymbs, entry.key(), "Saved entry key");
              assertEquals(entrySomeSymbs, parse(entry.value()), "Saved entry value");
            })
        .expectComplete()
        .verify();

    String entryKeyInt = String.valueOf(12345);
    ObjectNode entryValueInt = OBJECT_MAPPER.createObjectNode().put("value", 10);
    StepVerifier.create(
            configurationService
                .createEntry(
                    new CreateOrUpdateEntryRequest(apiKey, repoName, entryKeyInt, entryValueInt))
                .then(
                    configurationService.readEntry(
                        new ReadEntryRequest(apiKey, repoName, entryKeyInt, 1))))
        .assertNext(
            entry -> {
              assertEquals(entryKeyInt, entry.key(), "Saved entry key");
              assertEquals(entryValueInt, parse(entry.value()), "Saved entry value");
            })
        .expectComplete()
        .verify();
  }

  @Disabled("test is unstable in travis CI")
  @TestTemplate
  @DisplayName(
      "#11.1 Scenario: Successful entry creation (no validation for input) enabling to save "
          + "following values: "
          + "  - JsonArray")
  void createEntryWithDiffValuesNext(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String apiKey = createApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    configurationService
        .createRepository(new CreateRepositoryRequest(apiKey, repoName))
        .block(TIMEOUT);

    String entryKeyBlankJsonArray = "blankJsonArray";
    ObjectNode entryValueBlankJsonArray =
        OBJECT_MAPPER.createObjectNode().put("value", JsonArray.empty().toString());
    StepVerifier.create(
            configurationService
                .createEntry(
                    new CreateOrUpdateEntryRequest(
                        apiKey, repoName, entryKeyBlankJsonArray, entryValueBlankJsonArray))
                .then(
                    configurationService.readEntry(
                        new ReadEntryRequest(apiKey, repoName, entryKeyBlankJsonArray, 1))))
        .assertNext(
            entry -> {
              assertEquals(entryKeyBlankJsonArray, entry.key(), "Saved entry key");
              assertEquals(entryValueBlankJsonArray, parse(entry.value()), "Saved entry value");
            })
        .expectComplete()
        .verify();

    String entryKeyDataJsonArray = "dataJsonArray";
    ObjectNode entryValueDataJsonArray =
        OBJECT_MAPPER
            .createObjectNode()
            .put("value", JsonArray.create().add(99).add("some string").toString());
    StepVerifier.create(
            configurationService
                .createEntry(
                    new CreateOrUpdateEntryRequest(
                        apiKey, repoName, entryKeyDataJsonArray, entryValueDataJsonArray))
                .then(
                    configurationService.readEntry(
                        new ReadEntryRequest(apiKey, repoName, entryKeyDataJsonArray, 1))))
        .assertNext(
            entry -> {
              assertEquals(entryKeyDataJsonArray, entry.key(), "Saved entry key");
              assertEquals(entryValueDataJsonArray, parse(entry.value()), "Saved entry value");
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#11.2 Scenario: Successful entry creation (no validation for input) enabling to save "
          + "following values: "
          + "  - null / empty string")
  void createEntryWithDiffValuesNextSplit(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String apiKey = createApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    configurationService
        .createRepository(new CreateRepositoryRequest(apiKey, repoName))
        .block(TIMEOUT);

    String entryKeyBlankJsonObject = "blankJsonObject";
    ObjectNode entryValueBlankJsonObject =
        OBJECT_MAPPER.createObjectNode().put("value", JsonObject.empty().toString());
    StepVerifier.create(
            configurationService
                .createEntry(
                    new CreateOrUpdateEntryRequest(
                        apiKey, repoName, entryKeyBlankJsonObject, entryValueBlankJsonObject))
                .then(
                    configurationService.readEntry(
                        new ReadEntryRequest(apiKey, repoName, entryKeyBlankJsonObject, 1))))
        .assertNext(
            entry -> {
              assertEquals(entryKeyBlankJsonObject, entry.key(), "Saved entry key");
              assertEquals(entryValueBlankJsonObject, parse(entry.value()), "Saved entry value");
            })
        .expectComplete()
        .verify();

    String entryKeyUndef = null;
    ObjectNode entryValueUndef = OBJECT_MAPPER.createObjectNode().putNull("value");
    StepVerifier.create(
            configurationService
                .createEntry(
                    new CreateOrUpdateEntryRequest(
                        apiKey, repoName, entryKeyUndef, entryValueUndef))
                .then(
                    configurationService.readEntry(
                        new ReadEntryRequest(apiKey, repoName, entryKeyUndef, 1))))
        .expectErrorMessage("Please specify 'key'")
        .verify();

    String entryKeyEmpty = "";
    ObjectNode entryValueEmpty = OBJECT_MAPPER.createObjectNode();
    StepVerifier.create(
            configurationService
                .createEntry(
                    new CreateOrUpdateEntryRequest(
                        apiKey, repoName, entryKeyEmpty, entryValueEmpty))
                .then(
                    configurationService.readEntry(
                        new ReadEntryRequest(apiKey, repoName, entryKeyEmpty, 1))))
        .expectErrorMessage("Please specify 'key'")
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#12 Scenario: Fail to createEntry due to restricted permission upon the \"Member\" API key was applied")
  void createEntryByMember(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String ownerApiKey = createApiKey(organizationService, orgId, Role.Owner).key();
    String memberApiKey = createApiKey(organizationService, orgId, Role.Member).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-CURRENCY-999";
    ObjectNode entryValue =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "JPY")
            .put("name", "Yen")
            .put("DecimalPrecision", 4)
            .put("Rounding", "down");

    configurationService
        .createRepository(new CreateRepositoryRequest(ownerApiKey, repoName))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(memberApiKey, repoName, entryKey, entryValue)))
        .expectErrorMessage("Permission denied")
        .verify();
  }

  @TestTemplate
  @DisplayName("#13 Scenario: Fail to createEntry due to Key name duplication")
  void createEntryWithKeyNameDuplication(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String apiKey = createApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";
    ObjectNode entryValue =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "XAG")
            .put("name", "Silver")
            .put("DecimalPrecision", 4)
            .put("Rounding", "down");

    configurationService
        .createRepository(new CreateRepositoryRequest(apiKey, repoName))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService
                .createEntry(new CreateOrUpdateEntryRequest(apiKey, repoName, entryKey, entryValue))
                .then(
                    configurationService.readEntry(
                        new ReadEntryRequest(apiKey, repoName, entryKey))))
        .assertNext(
            entry -> {
              assertEquals(entryKey, entry.key(), "Saved entry key");
              assertEquals(entryValue, parse(entry.value()), "Saved entry value");
            })
        .expectComplete()
        .verify();

    StepVerifier.create(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(apiKey, repoName, entryKey, entryValue)))
        .expectErrorMessage(
            String.format(REPOSITORY_KEY_ALREADY_EXISTS_FORMATTER, repoName, entryKey))
        .verify();
  }

  @TestTemplate
  @DisplayName("#14 Scenario: Fail to createEntry due to specified Repository doesn't exist")
  void createEntryForNonExistingRepository(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String adminApiKey = createApiKey(organizationService, orgId, Role.Admin).key();

    String nonExistingRepoName = "NON-EXISTING-REPO";
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";
    ObjectNode entryValue =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "XAG")
            .put("name", "Silver")
            .put("DecimalPrecision", 4)
            .put("Rounding", "down");

    StepVerifier.create(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(
                    adminApiKey, nonExistingRepoName, entryKey, entryValue)))
        .expectErrorMessage(String.format(REPOSITORY_NOT_FOUND_FORMATTER, nonExistingRepoName))
        .verify();
  }

  @TestTemplate
  @DisplayName("#15 Scenario: Fail to createEntry upon the Owner deleted the \"Organization\"")
  void createEntryForDeletedOrganization(
      ConfigurationService configurationService, OrganizationService organizationService)
      throws InterruptedException {
    String orgId = createOrganization(organizationService).id();
    String apiKey = createApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";
    ObjectNode entryValue =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "XAG")
            .put("name", "Silver")
            .put("DecimalPrecision", 4)
            .put("Rounding", "down");

    configurationService
        .createRepository(new CreateRepositoryRequest(apiKey, repoName))
        .block(TIMEOUT);

    organizationService
        .deleteOrganization(new DeleteOrganizationRequest(AUTH0_TOKEN, orgId))
        .block(TIMEOUT);

    TimeUnit.SECONDS.sleep(KEY_CACHE_TTL + 1);

    StepVerifier.create(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(apiKey, repoName, entryKey, entryValue)))
        .expectErrorMessage(TOKEN_VERIFICATION_FAILED)
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#16 Scenario: Fail to createEntry upon the \"Admin\" apiKey was deleted from the Organization")
  void createEntryUsingDeletedApiKey(
      ConfigurationService configurationService, OrganizationService organizationService)
      throws InterruptedException {
    String orgId = createOrganization(organizationService).id();
    ApiKey apiKey = createApiKey(organizationService, orgId, Role.Owner);

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";
    ObjectNode entryValue =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "XAG")
            .put("name", "Silver")
            .put("DecimalPrecision", 4)
            .put("Rounding", "down");

    configurationService
        .createRepository(new CreateRepositoryRequest(apiKey.key(), repoName))
        .block(TIMEOUT);

    organizationService
        .deleteOrganizationApiKey(
            new DeleteOrganizationApiKeyRequest(AUTH0_TOKEN, orgId, apiKey.name()))
        .block(TIMEOUT);

    TimeUnit.SECONDS.sleep(KEY_CACHE_TTL + 1);

    StepVerifier.create(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(apiKey.key(), repoName, entryKey, entryValue)))
        .expectErrorMessage(TOKEN_VERIFICATION_FAILED)
        .verify();
  }

  @TestTemplate
  @DisplayName("#17 Scenario: Fail to createEntry due to invalid apiKey was applied")
  void createEntryUsingExpiredApiKey(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String apiKey = getExpiredApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";
    ObjectNode entryValue =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "XAG")
            .put("name", "Silver")
            .put("DecimalPrecision", 4)
            .put("Rounding", "down");

    StepVerifier.create(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(apiKey, repoName, entryKey, entryValue)))
        .expectErrorMessage(TOKEN_VERIFICATION_FAILED)
        .verify();
  }

  @TestTemplate
  @DisplayName("#18 Scenario: Fail to createEntry with empty or undefined apiKey")
  void createEntryWithEmptyOrUndefinedApiKey(ConfigurationService configurationService) {

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "new-key";
    ObjectNode entryValue = OBJECT_MAPPER.createObjectNode();

    StepVerifier.create(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(null, repoName, entryKey, entryValue)))
        .expectErrorMessage(PLEASE_SPECIFY_API_KEY)
        .verify();

    StepVerifier.create(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest("", repoName, entryKey, entryValue)))
        .expectErrorMessage("Please specify 'apiKey'")
        .verify();
  }

  @TestTemplate
  @DisplayName("#19 Scenario: Fail to createEntry with empty or undefined Repository name")
  void createEntryWithEmptyOrUndefinedRepoName(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String apiKey = getExpiredApiKey(organizationService, orgId, Role.Owner).key();

    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";
    ObjectNode entryValue =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "XAG")
            .put("name", "Silver")
            .put("DecimalPrecision", 4)
            .put("Rounding", "down");

    StepVerifier.create(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(apiKey, null, entryKey, entryValue)))
        .expectErrorMessage(PLEASE_SPECIFY_REPO)
        .verify();

    StepVerifier.create(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(apiKey, "", entryKey, entryValue)))
        .expectErrorMessage(PLEASE_SPECIFY_REPO)
        .verify();
  }

  @TestTemplate
  @DisplayName("#20 Scenario: Fail to createEntry with empty or undefined Key field")
  void createEntryWithEmptyOrUndefinedKey(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String apiKey = getExpiredApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    ObjectNode entryValue =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "XAG")
            .put("name", "Silver")
            .put("DecimalPrecision", 4)
            .put("Rounding", "down");

    StepVerifier.create(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(apiKey, repoName, null, entryValue)))
        .expectErrorMessage(PLEASE_SPECIFY_KEY)
        .verify();

    StepVerifier.create(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(apiKey, repoName, "", entryValue)))
        .expectErrorMessage(PLEASE_SPECIFY_KEY)
        .verify();
  }
}
