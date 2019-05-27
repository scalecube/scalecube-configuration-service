package io.scalecube.configuration.scenario;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.scalecube.account.api.ApiKey;
import io.scalecube.account.api.DeleteOrganizationApiKeyRequest;
import io.scalecube.account.api.DeleteOrganizationRequest;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Role;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.CreateRepositoryRequest;
import io.scalecube.configuration.api.EntriesRequest;
import io.scalecube.configuration.api.FetchRequest;
import io.scalecube.configuration.api.FetchResponse;
import io.scalecube.configuration.api.InvalidAuthenticationToken;
import io.scalecube.configuration.api.SaveRequest;
import io.scalecube.configuration.repository.exception.RepositoryNotFoundException;
import java.security.AccessControlException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import reactor.test.StepVerifier;

public class SaveEntryScenario extends BaseScenario {

  @TestTemplate
  @DisplayName("#7 Successful save of specific entry (instrument) applying the \"Owner\" API key")
  void saveEntry(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = getOrganization(organizationService, ORGANIZATION_1).id();
    String token = getApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = "test-repo";
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
                .save(new SaveRequest(token, repoName, entryKey, entryValue))
                .then(configurationService.fetch(new FetchRequest(token, repoName, entryKey))))
        .assertNext(
            entry -> {
              assertEquals(entryKey, entry.key(), "Saved entry key");
              assertEquals(entryValue, entry.value(), "Saved entry value");
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#8 Successful save the identical entries for different Repositories applying the \"Admin\" API key")
  void saveEntryToDifferentRepositories(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = getOrganization(organizationService, ORGANIZATION_1).id();
    String ownerToken = getApiKey(organizationService, orgId, Role.Owner).key();
    String adminToken = getApiKey(organizationService, orgId, Role.Admin).key();

    String repoName1 = "test-repo1";
    String repoName2 = "test-repo2";
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
                .save(new SaveRequest(adminToken, repoName1, entryKey, entryValue))
                .then(
                    configurationService.fetch(new FetchRequest(adminToken, repoName1, entryKey))))
        .assertNext(
            entry -> {
              assertEquals(entryKey, entry.key(), "Saved entry key");
              assertEquals(entryValue, entry.value(), "Saved entry value");
            })
        .expectComplete()
        .verify();

    StepVerifier.create(
            configurationService
                .save(new SaveRequest(adminToken, repoName2, entryKey, entryValue))
                .then(
                    configurationService.fetch(new FetchRequest(adminToken, repoName2, entryKey))))
        .assertNext(
            entry -> {
              assertEquals(entryKey, entry.key(), "Saved entry key");
              assertEquals(entryValue, entry.value(), "Saved entry value");
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#9 Successful update one of the identical entries in the single Repository applying the \"Owner\" API key")
  void updateEntry(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = getOrganization(organizationService, ORGANIZATION_1).id();
    String token = getApiKey(organizationService, orgId, Role.Owner).key();

    String repoName1 = "test-repo1";
    String repoName2 = "test-repo2";
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
        .then(configurationService.save(new SaveRequest(token, repoName1, entryKey, entryValue1)))
        .block(TIMEOUT);

    configurationService
        .createRepository(new CreateRepositoryRequest(token, repoName2))
        .then(configurationService.save(new SaveRequest(token, repoName2, entryKey, entryValue1)))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService
                .save(new SaveRequest(token, repoName1, entryKey, entryValue2))
                .then(configurationService.fetch(new FetchRequest(token, repoName1, entryKey))))
        .assertNext(
            entry -> {
              assertEquals(entryKey, entry.key(), "Saved entry key");
              assertEquals(entryValue2, entry.value(), "Saved entry value");
            })
        .expectComplete()
        .verify();

    StepVerifier.create(configurationService.fetch(new FetchRequest(token, repoName2, entryKey)))
        .assertNext(
            entry -> {
              assertEquals(entryKey, entry.key(), "Saved entry key");
              assertEquals(entryValue1, entry.value(), "Saved entry value");
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#10 No change for the successful update of the existing entry with the same values applying the \"Admin\" API key")
  void saveEntryTwice(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = getOrganization(organizationService, ORGANIZATION_1).id();
    String ownerToken = getApiKey(organizationService, orgId, Role.Owner).key();
    String adminToken = getApiKey(organizationService, orgId, Role.Admin).key();

    String repoName = "test-repo";
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
            configurationService.save(new SaveRequest(ownerToken, repoName, entryKey, entryValue)))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService
                .save(new SaveRequest(adminToken, repoName, entryKey, entryValue))
                .then(configurationService.entries(new EntriesRequest(adminToken, repoName))))
        .assertNext(
            entries -> {
              assertEquals(1, entries.size(), "Entries in repository");

              FetchResponse entry = entries.get(0);
              assertEquals(entryKey, entry.key(), "Saved entry key");
              assertEquals(entryValue, entry.value(), "Saved entry value");
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#11 Successful save the specific entries applying the \"Owner\" API key for:\n"
          + "  - values that reach at least a 1000 chars (no quantity validation for input)\n"
          + "  - values which chars are symbols and spaces (no chars validation for input)")
  void saveEntryWith1000charsAndSpecialSymbols(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = getOrganization(organizationService, ORGANIZATION_1).id();
    String token = getApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = "test-repo";
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
                .save(new SaveRequest(token, repoName, entryKey, entryValue))
                .then(configurationService.fetch(new FetchRequest(token, repoName, entryKey))))
        .assertNext(
            entry -> {
              assertEquals(entryKey, entry.key(), "Saved entry key");
              assertEquals(entryValue, entry.value(), "Saved entry value");
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#12 Fail to save a specific entry upon the restricted permission due to applying the \"Member\" API key")
  void saveEntryByMember(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = getOrganization(organizationService, ORGANIZATION_1).id();
    String ownerToken = getApiKey(organizationService, orgId, Role.Owner).key();
    String memberToken = getApiKey(organizationService, orgId, Role.Member).key();

    String repoName = "test-repo";
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
            configurationService.save(new SaveRequest(memberToken, repoName, entryKey, entryValue)))
        .expectErrorSatisfies(
            e -> {
              assertEquals(AccessControlException.class, e.getClass());
              assertEquals("Permission denied", e.getMessage());
            })
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#13 Fail to save (edit) the specific entry applying the \"Admin\" either \"Owner\" API key upon the specified Repository doesn't exist")
  void saveEntryToNonExistingRepository(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = getOrganization(organizationService, ORGANIZATION_1).id();
    String adminToken = getApiKey(organizationService, orgId, Role.Admin).key();

    String nonExistingRepoName = "test-repo";
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";
    ObjectNode entryValue =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "XAG")
            .put("name", "Silver")
            .put("DecimalPrecision", 4)
            .put("Rounding", "down");

    StepVerifier.create(
            configurationService.save(
                new SaveRequest(adminToken, nonExistingRepoName, entryKey, entryValue)))
        .expectErrorSatisfies(
            e -> {
              assertEquals(RepositoryNotFoundException.class, e.getClass());
              assertEquals(
                  String.format("Repository '%s-%s' not found", orgId, nonExistingRepoName),
                  e.getMessage());
            })
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#14 Fail to save (edit) the specific entry in the Repository upon the \"token\" is invalid (expired)")
  void saveEntryUsingExpiredToken(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = getOrganization(organizationService, ORGANIZATION_1).id();
    String token = getExpiredApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = "test-repo";
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";
    ObjectNode entryValue =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "XAG")
            .put("name", "Silver")
            .put("DecimalPrecision", 4)
            .put("Rounding", "down");

    StepVerifier.create(
            configurationService.save(new SaveRequest(token, repoName, entryKey, entryValue)))
        .expectErrorSatisfies(
            e -> {
              assertEquals(InvalidAuthenticationToken.class, e.getClass());
              assertEquals("Token verification failed", e.getMessage());
            })
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#15 Fail to save (edit) the specific entry in the Repository upon the Owner deleted the Organization with related \"Owner\" API key")
  void saveEntryForDeletedOrganization(
      ConfigurationService configurationService, OrganizationService organizationService)
      throws InterruptedException {
    String orgId = getOrganization(organizationService, ORGANIZATION_1).id();
    String token = getApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = "test-repo";
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
        .deleteOrganization(new DeleteOrganizationRequest(AUTH0_TOKEN, "ORG-TEST"))
        .block(TIMEOUT);

    TimeUnit.SECONDS.sleep(3);

    StepVerifier.create(
            configurationService.save(new SaveRequest(token, repoName, entryKey, entryValue)))
        .expectErrorSatisfies(
            e -> {
              assertEquals(InvalidAuthenticationToken.class, e.getClass());
              assertEquals("Token verification failed", e.getMessage());
            })
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#16 Fail to save (edit) the specific entry in the Repository upon the Owner applied some of the manager's API key from another Organization")
  void saveEntryUsingTokenOfAnotherOrganization(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId1 = getOrganization(organizationService, ORGANIZATION_1).id();
    String token1 = getApiKey(organizationService, orgId1, Role.Owner).key();

    String orgId2 = getOrganization(organizationService, ORGANIZATION_2).id();
    String token2 = getApiKey(organizationService, orgId2, Role.Admin).key();

    String repoName = "test-repo";
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
            configurationService.save(new SaveRequest(token2, repoName, entryKey, entryValue)))
        .expectErrorSatisfies(
            e -> {
              assertEquals(RepositoryNotFoundException.class, e.getClass());
              assertEquals(
                  String.format("Repository '%s-%s' not found", orgId2, repoName), e.getMessage());
            })
        .verify();
  }

  @Disabled("Feature is not implemented")
  @TestTemplate
  @DisplayName(
      "#17 Fail to save (edit) the specific entry in the Repository upon the Owner \"token\" (API key) was deleted from the Organization")
  void saveEntryUsingDeletedToken(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = getOrganization(organizationService, ORGANIZATION_1).id();
    ApiKey token = getApiKey(organizationService, orgId, Role.Owner);

    String repoName = "test-repo";
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
        .deleteOrganizationApiKey(
            new DeleteOrganizationApiKeyRequest(AUTH0_TOKEN, orgId, token.name()))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService.save(new SaveRequest(token, repoName, entryKey, entryValue)))
        .expectErrorSatisfies(
            e -> {
              assertEquals(InvalidAuthenticationToken.class, e.getClass());
              assertEquals("Token verification failed", e.getMessage());
            })
        .verify();
  }
}
