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
import io.scalecube.configuration.api.ReadEntryRequest;
import io.scalecube.configuration.api.ReadEntryResponse;
import io.scalecube.configuration.api.ReadListRequest;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;
import reactor.test.StepVerifier;

public class CreateEntryScenario extends BaseScenario {

  @TestTemplate
  @DisplayName("#7 Successful create of specific entry (instrument) applying the \"Owner\" API key")
  void createEntry(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String token = createApiKey(organizationService, orgId, Role.Owner).key();

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
        .createRepository(new CreateRepositoryRequest(token, repoName))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService
                .createEntry(new CreateOrUpdateEntryRequest(token, repoName, entryKey, entryValue))
                .then(configurationService.readEntry(new ReadEntryRequest(token, repoName, entryKey))))
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
      "#8 Successful creation the identical entries for different Repositories applying the \"Admin\" API key")
  void createEntryForDifferentRepositories(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String ownerToken = createApiKey(organizationService, orgId, Role.Owner).key();
    String adminToken = createApiKey(organizationService, orgId, Role.Admin).key();

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
        .createRepository(new CreateRepositoryRequest(ownerToken, repoName1))
        .block(TIMEOUT);

    configurationService
        .createRepository(new CreateRepositoryRequest(ownerToken, repoName2))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService
                .createEntry(new CreateOrUpdateEntryRequest(adminToken, repoName1, entryKey, entryValue))
                .then(
                    configurationService.readEntry(new ReadEntryRequest(adminToken, repoName1, entryKey))))
        .assertNext(
            entry -> {
              assertEquals(entryKey, entry.key(), "Saved entry key");
              assertEquals(entryValue, parse(entry.value()), "Saved entry value");
            })
        .expectComplete()
        .verify();

    StepVerifier.create(
            configurationService
                .createEntry(new CreateOrUpdateEntryRequest(adminToken, repoName2, entryKey, entryValue))
                .then(
                    configurationService.readEntry(new ReadEntryRequest(adminToken, repoName2, entryKey))))
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
      "#9 Successful update one of the identical entries in the single Repository applying the \"Owner\" API key")
  void updateEntry(
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
        .then(configurationService.createEntry(new CreateOrUpdateEntryRequest(token, repoName1, entryKey, entryValue1)))
        .block(TIMEOUT);

    configurationService
        .createRepository(new CreateRepositoryRequest(token, repoName2))
        .then(configurationService.createEntry(new CreateOrUpdateEntryRequest(token, repoName2, entryKey, entryValue1)))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService
                .createEntry(new CreateOrUpdateEntryRequest(token, repoName1, entryKey, entryValue2))
                .then(configurationService.readEntry(new ReadEntryRequest(token, repoName1, entryKey))))
        .assertNext(
            entry -> {
              assertEquals(entryKey, entry.key(), "Saved entry key");
              assertEquals(entryValue2, parse(entry.value()), "Saved entry value");
            })
        .expectComplete()
        .verify();

    StepVerifier.create(configurationService.readEntry(new ReadEntryRequest(token, repoName2, entryKey)))
        .assertNext(
            entry -> {
              assertEquals(entryKey, entry.key(), "Saved entry key");
              assertEquals(entryValue1, parse(entry.value()), "Saved entry value");
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#10 No change for the successful update of the existing entry with the same values applying the \"Admin\" API key")
  void createEntryTwice(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String ownerToken = createApiKey(organizationService, orgId, Role.Owner).key();
    String adminToken = createApiKey(organizationService, orgId, Role.Admin).key();

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
        .createRepository(new CreateRepositoryRequest(ownerToken, repoName))
        .then(
            configurationService.createEntry(new CreateOrUpdateEntryRequest(ownerToken, repoName, entryKey, entryValue)))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService
                .createEntry(new CreateOrUpdateEntryRequest(adminToken, repoName, entryKey, entryValue))
                .then(configurationService.readList(new ReadListRequest(adminToken, repoName))))
        .assertNext(
            entries -> {
              assertEquals(1, entries.size(), "Entries in repository");

              ReadEntryResponse entry = entries.get(0);
              assertEquals(entryKey, entry.key(), "Saved entry key");
              assertEquals(entryValue, parse(entry.value()), "Saved entry value");
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#11 Successful creation the specific entries applying the \"Owner\" API key for:\n"
          + "  - values that reach at least a 1000 chars (no quantity validation for input)\n"
          + "  - values which chars are symbols and spaces (no chars validation for input)")
  void createEntryWith1000CharsAndSpecialSymbols(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String token = createApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";
    ObjectNode entryValue =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", RandomStringUtils.randomAscii(1000))
            .put("name", RandomStringUtils.randomAscii(1000))
            .put("DecimalPrecision", RandomStringUtils.randomAscii(1000))
            .put("Rounding", RandomStringUtils.randomAscii(1000));

    configurationService
        .createRepository(new CreateRepositoryRequest(token, repoName))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService
                .createEntry(new CreateOrUpdateEntryRequest(token, repoName, entryKey, entryValue))
                .then(configurationService.readEntry(new ReadEntryRequest(token, repoName, entryKey))))
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
      "#12 Fail to creation a specific entry upon the restricted permission due to applying the \"Member\" API key")
  void createEntryByMember(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String ownerToken = createApiKey(organizationService, orgId, Role.Owner).key();
    String memberToken = createApiKey(organizationService, orgId, Role.Member).key();

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
        .createRepository(new CreateRepositoryRequest(ownerToken, repoName))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService.createEntry(new CreateOrUpdateEntryRequest(memberToken, repoName, entryKey, entryValue)))
        .expectErrorMessage("Permission denied")
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#13 Fail to create (edit) the specific entry applying the \"Admin\" either \"Owner\" API key upon the specified Repository doesn't exist")
  void createEntryForNonExistingRepository(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String adminToken = createApiKey(organizationService, orgId, Role.Admin).key();

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
                new CreateOrUpdateEntryRequest(adminToken, nonExistingRepoName, entryKey, entryValue)))
        .expectErrorMessage(
            String.format("Repository '%s-%s' not found", orgId, nonExistingRepoName))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#14 Fail to creation (edit) the specific entry in the Repository upon the \"apiKey\" is invalid (expired)")
  void createEntryUsingExpiredToken(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String token = getExpiredApiKey(organizationService, orgId, Role.Owner).key();

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
            configurationService.createEntry(new CreateOrUpdateEntryRequest(token, repoName, entryKey, entryValue)))
        .expectErrorMessage("Token verification failed")
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#15 Fail to create (edit) the specific entry in the Repository upon the Owner deleted the Organization with related \"Owner\" API key")
  void createEntryForDeletedOrganization(
      ConfigurationService configurationService, OrganizationService organizationService)
      throws InterruptedException {
    String orgId = createOrganization(organizationService).id();
    String token = createApiKey(organizationService, orgId, Role.Owner).key();

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
        .createRepository(new CreateRepositoryRequest(token, repoName))
        .block(TIMEOUT);

    organizationService
        .deleteOrganization(new DeleteOrganizationRequest(AUTH0_TOKEN, orgId))
        .block(TIMEOUT);

    TimeUnit.SECONDS.sleep(KEY_CACHE_TTL + 1);

    StepVerifier.create(
            configurationService.createEntry(new CreateOrUpdateEntryRequest(token, repoName, entryKey, entryValue)))
        .expectErrorMessage("Token verification failed")
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#16 Fail to creation (edit) the specific entry in the Repository upon the Owner applied some of the manager's API key from another Organization")
  void createEntryUsingTokenOfAnotherOrganization(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId1 = createOrganization(organizationService).id();
    String token1 = createApiKey(organizationService, orgId1, Role.Owner).key();

    String orgId2 = createOrganization(organizationService).id();
    String token2 = createApiKey(organizationService, orgId2, Role.Admin).key();

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
        .createRepository(new CreateRepositoryRequest(token1, repoName))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService.createEntry(new CreateOrUpdateEntryRequest(token2, repoName, entryKey, entryValue)))
        .expectErrorMessage(String.format("Repository '%s-%s' not found", orgId2, repoName))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#17 Fail to creation (edit) the specific entry in the Repository upon the Owner \"apiKey\" (API key) was deleted from the Organization")
  void createEntryUsingDeletedToken(
      ConfigurationService configurationService, OrganizationService organizationService)
      throws InterruptedException {
    String orgId = createOrganization(organizationService).id();
    ApiKey token = createApiKey(organizationService, orgId, Role.Owner);

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
        .createRepository(new CreateRepositoryRequest(token.key(), repoName))
        .block(TIMEOUT);

    organizationService
        .deleteOrganizationApiKey(
            new DeleteOrganizationApiKeyRequest(AUTH0_TOKEN, orgId, token.name()))
        .block(TIMEOUT);

    TimeUnit.SECONDS.sleep(KEY_CACHE_TTL + 1);

    StepVerifier.create(
            configurationService.createEntry(new CreateOrUpdateEntryRequest(token.key(), repoName, entryKey, entryValue)))
        .expectErrorMessage("Token verification failed")
        .verify();
  }
}
