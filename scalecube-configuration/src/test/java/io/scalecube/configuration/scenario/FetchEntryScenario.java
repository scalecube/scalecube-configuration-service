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
import io.scalecube.configuration.api.InvalidAuthenticationToken;
import io.scalecube.configuration.api.SaveRequest;
import io.scalecube.configuration.fixtures.InMemoryConfigurationServiceFixture;
import io.scalecube.configuration.repository.exception.KeyNotFoundException;
import io.scalecube.configuration.repository.exception.RepositoryNotFoundException;
import io.scalecube.test.fixtures.Fixtures;
import io.scalecube.test.fixtures.WithFixture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import reactor.test.StepVerifier;

@ExtendWith(Fixtures.class)
public class FetchEntryScenario extends BaseScenario {

  @TestTemplate
  @DisplayName(
      "#18 Successful get of a specific entry from the related Repository applying the all related API keys: \"Owner\", \"Admin\", \"Member\"")
  void fetchEntry(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = getOrganization(organizationService, ORGANIZATION_1).id();
    String ownerToken = getApiKey(organizationService, orgId, Role.Owner).key();
    String adminToken = getApiKey(organizationService, orgId, Role.Admin).key();
    String memberToken = getApiKey(organizationService, orgId, Role.Member).key();

    String repoName = "test-repo";
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
            configurationService.save(
                new SaveRequest(ownerToken, repoName, entryKey1, entryValue1)))
        .then(
            configurationService.save(
                new SaveRequest(ownerToken, repoName, entryKey2, entryValue2)))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService.fetch(new FetchRequest(ownerToken, repoName, entryKey1)))
        .assertNext(
            entry -> {
              assertEquals(entryKey1, entry.key(), "Fetched entry key");
              assertEquals(entryValue1, entry.value(), "Fetched entry value");
            })
        .expectComplete()
        .verify();

    StepVerifier.create(
            configurationService.fetch(new FetchRequest(adminToken, repoName, entryKey1)))
        .assertNext(
            entry -> {
              assertEquals(entryKey1, entry.key(), "Fetched entry key");
              assertEquals(entryValue1, entry.value(), "Fetched entry value");
            })
        .expectComplete()
        .verify();

    StepVerifier.create(
            configurationService.fetch(new FetchRequest(memberToken, repoName, entryKey1)))
        .assertNext(
            entry -> {
              assertEquals(entryKey1, entry.key(), "Fetched entry key");
              assertEquals(entryValue1, entry.value(), "Fetched entry value");
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#19 Successful get one of the identical entries from the related Repository applying some of the related API keys: \"Owner\", \"Admin\", \"Member\"")
  void fetchIdenticalEntry(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = getOrganization(organizationService, ORGANIZATION_1).id();
    String ownerToken = getApiKey(organizationService, orgId, Role.Owner).key();
    String memberToken = getApiKey(organizationService, orgId, Role.Member).key();

    String repoName1 = "test-repo1";
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
            configurationService.save(
                new SaveRequest(ownerToken, repoName1, entryKey1, entryValue1)))
        .then(
            configurationService.save(
                new SaveRequest(ownerToken, repoName2, entryKey1, entryValue2)))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService.fetch(new FetchRequest(memberToken, repoName1, entryKey1)))
        .assertNext(
            entry -> {
              assertEquals(entryKey1, entry.key(), "Fetched entry key");
              assertEquals(entryValue1, entry.value(), "Fetched entry value");
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#20 Fail to get the non-existent entry from the existent Repository applying some of the accessible API keys: \"Owner\", \"Admin\", \"Member\"")
  void fetchNonExistentEntry(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = getOrganization(organizationService, ORGANIZATION_1).id();
    String ownerToken = getApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = "test-repo";
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
            configurationService.save(
                new SaveRequest(ownerToken, repoName, entryKey1, entryValue1)))
        .then(
            configurationService.save(
                new SaveRequest(ownerToken, repoName, entryKey2, entryValue2)))
        .block(TIMEOUT);

    String nonExistentKey = "NON_EXISTENT_KEY";

    StepVerifier.create(
            configurationService.fetch(new FetchRequest(ownerToken, repoName, nonExistentKey)))
        .expectErrorSatisfies(
            e -> {
              assertEquals(KeyNotFoundException.class, e.getClass());
              assertEquals(String.format("Key '%s' not found", nonExistentKey), e.getMessage());
            })
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#21 Fail to get any entry from the non-existent Repository applying some of the accessible API keys: \"Owner\", \"Admin\", \"Member\"")
  void fetchEntryFromNonExistentRepository(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = getOrganization(organizationService, ORGANIZATION_1).id();
    String token = getApiKey(organizationService, orgId, Role.Admin).key();

    String repoName = "NON_EXISTENT_REPO";

    StepVerifier.create(configurationService.fetch(new FetchRequest(token, repoName, "key")))
        .expectErrorSatisfies(
            e -> {
              assertEquals(RepositoryNotFoundException.class, e.getClass());
              assertEquals(
                  String.format("Repository '%s-%s' not found", orgId, repoName), e.getMessage());
            })
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#22 Fail to get the specific entry from the Repository upon the \"token\" is invalid (expired)")
  void fetchEntryUsingExpiredToken(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = getOrganization(organizationService, ORGANIZATION_1).id();
    String token = getExpiredApiKey(organizationService, orgId, Role.Owner).key();

    StepVerifier.create(configurationService.entries(new EntriesRequest(token, "test-repo")))
        .expectErrorSatisfies(
            e -> {
              assertEquals(InvalidAuthenticationToken.class, e.getClass());
              assertEquals("Token verification failed", e.getMessage());
            })
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#23 Fail to get the specific entry from the Repository upon the Owner deleted the Organization with related \"Admin\" API key")
  void fetchEntryForDeletedOrganization(
      ConfigurationService configurationService, OrganizationService organizationService)
      throws InterruptedException {
    String orgId = getOrganization(organizationService, ORGANIZATION_1).id();
    String ownerToken = getApiKey(organizationService, orgId, Role.Owner).key();
    String adminToken = getApiKey(organizationService, orgId, Role.Admin).key();

    String repoName = "test-repo";
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    configurationService
        .createRepository(new CreateRepositoryRequest(ownerToken, repoName))
        .then(
            configurationService.save(
                new SaveRequest(
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
        .deleteOrganization(new DeleteOrganizationRequest(AUTH0_TOKEN, "ORG-TEST"))
        .block(TIMEOUT);

    TimeUnit.SECONDS.sleep(3);

    StepVerifier.create(configurationService.entries(new EntriesRequest(adminToken, repoName)))
        .expectErrorSatisfies(
            e -> {
              assertEquals(InvalidAuthenticationToken.class, e.getClass());
              assertEquals("Token verification failed", e.getMessage());
            })
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#24 Fail to get the specific entry from the Repository upon the Owner applied some of the API keys from another Organization")
  void fetchEntryUsingTokenOfAnotherOrganization(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId1 = getOrganization(organizationService, ORGANIZATION_1).id();
    String token1 = getApiKey(organizationService, orgId1, Role.Owner).key();

    String orgId2 = getOrganization(organizationService, ORGANIZATION_2).id();
    String token2 = getApiKey(organizationService, orgId2, Role.Member).key();

    String repoName = "test-repo";
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    configurationService
        .createRepository(new CreateRepositoryRequest(token1, repoName))
        .then(
            configurationService.save(
                new SaveRequest(
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

    StepVerifier.create(configurationService.entries(new EntriesRequest(token2, repoName)))
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
      "#25 Fail to get the specific entry in the Repository upon the Admin \"token\" (API key) was deleted from the Organization")
  void fetchEntryUsingDeletedToken(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = getOrganization(organizationService, ORGANIZATION_1).id();
    ApiKey ownerToken = getApiKey(organizationService, orgId, Role.Owner);
    ApiKey adminToken = getApiKey(organizationService, orgId, Role.Admin);

    String repoName = "test-repo";
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    configurationService
        .createRepository(new CreateRepositoryRequest(ownerToken, repoName))
        .then(
            configurationService.save(
                new SaveRequest(
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
        .deleteOrganizationApiKey(
            new DeleteOrganizationApiKeyRequest(AUTH0_TOKEN, orgId, adminToken.name()))
        .block(TIMEOUT);

    StepVerifier.create(configurationService.entries(new EntriesRequest(adminToken, repoName)))
        .expectErrorSatisfies(
            e -> {
              assertEquals(InvalidAuthenticationToken.class, e.getClass());
              assertEquals("Token verification failed", e.getMessage());
            })
        .verify();
  }
}
