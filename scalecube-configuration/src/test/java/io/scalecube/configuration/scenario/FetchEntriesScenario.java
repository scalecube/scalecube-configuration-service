package io.scalecube.configuration.scenario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.scalecube.account.api.ApiKey;
import io.scalecube.account.api.DeleteOrganizationApiKeyRequest;
import io.scalecube.account.api.DeleteOrganizationRequest;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Role;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.CreateRepositoryRequest;
import io.scalecube.configuration.api.EntriesRequest;
import io.scalecube.configuration.api.FetchResponse;
import io.scalecube.configuration.api.SaveRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;
import reactor.test.StepVerifier;

public class FetchEntriesScenario extends BaseScenario {

  @TestTemplate
  @DisplayName(
      "#26 Successful get of the all existent entries list from the related Repository applying the all related API keys: \"Owner\", \"Admin\", \"Member\"")
  void fetchEntries(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String ownerToken = createApiKey(organizationService, orgId, Role.Owner).key();
    String adminToken = createApiKey(organizationService, orgId, Role.Admin).key();
    String memberToken = createApiKey(organizationService, orgId, Role.Member).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey1 = "KEY-FOR-PRECIOUS-METAL-123";
    String entryKey2 = "KEY-FOR-CURRENCY-999";

    configurationService
        .createRepository(new CreateRepositoryRequest(ownerToken, repoName))
        .then(
            configurationService.save(
                new SaveRequest(
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
            configurationService.save(
                new SaveRequest(
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

    StepVerifier.create(configurationService.entries(new EntriesRequest(ownerToken, repoName)))
        .assertNext(
            entries -> {
              assertEquals(2, entries.size(), "Fetched entries count");

              List<String> entriesKeys =
                  entries.stream().map(FetchResponse::key).collect(Collectors.toList());

              assertTrue(
                  entriesKeys.contains(entryKey1), "Entry " + entryKey1 + " found in response");
              assertTrue(
                  entriesKeys.contains(entryKey2), "Entry " + entryKey2 + " found in response");
            })
        .expectComplete()
        .verify();

    StepVerifier.create(configurationService.entries(new EntriesRequest(adminToken, repoName)))
        .assertNext(
            entries -> {
              assertEquals(2, entries.size(), "Fetched entries count");

              List<String> entriesKeys =
                  entries.stream().map(FetchResponse::key).collect(Collectors.toList());

              assertTrue(
                  entriesKeys.contains(entryKey1), "Entry " + entryKey1 + " found in response");
              assertTrue(
                  entriesKeys.contains(entryKey2), "Entry " + entryKey2 + " found in response");
            })
        .expectComplete()
        .verify();

    StepVerifier.create(configurationService.entries(new EntriesRequest(memberToken, repoName)))
        .assertNext(
            entries -> {
              assertEquals(2, entries.size(), "Fetched entries count");

              List<String> entriesKeys =
                  entries.stream().map(FetchResponse::key).collect(Collectors.toList());

              assertTrue(
                  entriesKeys.contains(entryKey1), "Entry " + entryKey1 + " found in response");
              assertTrue(
                  entriesKeys.contains(entryKey2), "Entry " + entryKey2 + " found in response");
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#27 Fail to get any entry from the non-existent Repository applying some of the accessible API keys: \"Owner\", \"Admin\", \"Member\"")
  void fetchEntriesFromNonExistentRepository(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String token = createApiKey(organizationService, orgId, Role.Admin).key();

    String repoName = "NON_EXISTENT_REPO";

    StepVerifier.create(configurationService.entries(new EntriesRequest(token, repoName)))
        .expectErrorMessage(String.format("Repository '%s-%s' not found", orgId, repoName))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#28 Fail to get any entry from the Repository upon the \"token\" is invalid (expired)")
  void fetchEntriesUsingExpiredToken(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String token = getExpiredApiKey(organizationService, orgId, Role.Owner).key();

    StepVerifier.create(configurationService.entries(new EntriesRequest(token, "test-repo")))
        .expectErrorMessage("Token verification failed")
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#29 Fail to get any entry from the Repository upon the Owner deleted the Organization with related \"Member\" API key")
  void fetchEntriesForDeletedOrganization(
      ConfigurationService configurationService, OrganizationService organizationService)
      throws InterruptedException {
    String orgId = createOrganization(organizationService).id();
    String ownerToken = createApiKey(organizationService, orgId, Role.Owner).key();
    String memberToken = createApiKey(organizationService, orgId, Role.Member).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
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
        .deleteOrganization(new DeleteOrganizationRequest(AUTH0_TOKEN, orgId))
        .block(TIMEOUT);

    TimeUnit.SECONDS.sleep(3);

    StepVerifier.create(configurationService.entries(new EntriesRequest(memberToken, repoName)))
        .expectErrorMessage("Token verification failed")
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#30 Fail to get any entry from the Repository upon the Owner applied some of the API keys from another Organization")
  void fetchEntriesUsingTokenOfAnotherOrganization(
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
        .expectErrorMessage(String.format("Repository '%s-%s' not found", orgId2, repoName))
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#31 Fail to get any entry from the Repository upon the Member \"token\" (API key) was deleted from the Organization")
  void fetchEntriesUsingDeletedToken(
      ConfigurationService configurationService, OrganizationService organizationService)
      throws InterruptedException {
    String orgId = createOrganization(organizationService).id();
    ApiKey ownerToken = createApiKey(organizationService, orgId, Role.Owner);
    ApiKey memberToken = createApiKey(organizationService, orgId, Role.Member);

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    configurationService
        .createRepository(new CreateRepositoryRequest(ownerToken.key(), repoName))
        .then(
            configurationService.save(
                new SaveRequest(
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
            new DeleteOrganizationApiKeyRequest(AUTH0_TOKEN, orgId, memberToken.name()))
        .block(TIMEOUT);

    TimeUnit.SECONDS.sleep(3);

    StepVerifier.create(
            configurationService.entries(new EntriesRequest(memberToken.key(), repoName)))
        .expectErrorMessage("Token verification failed")
        .verify();
  }
}
