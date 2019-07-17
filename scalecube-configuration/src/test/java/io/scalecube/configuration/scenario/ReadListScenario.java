package io.scalecube.configuration.scenario;

import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Role;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.CreateOrUpdateEntryRequest;
import io.scalecube.configuration.api.CreateRepositoryRequest;
import io.scalecube.configuration.api.ReadEntryRequest;
import io.scalecube.configuration.api.ReadListRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

public class ReadListScenario extends BaseScenario {

  @TestTemplate
  @DisplayName(
      "#53 Scenario: Successful readList (latest key versions) from the related Repository")
  void readEntries(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String ownerApiKey = createApiKey(organizationService, orgId, Role.Owner).key();
    String adminApiKey = createApiKey(organizationService, orgId, Role.Admin).key();
    String memberApiKey = createApiKey(organizationService, orgId, Role.Member).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);
    String entryKey1 = "KEY-FOR-PRECIOUS-METAL-123";
    String entryKey2 = "KEY-FOR-CURRENCY-999";

    configurationService
        .createRepository(new CreateRepositoryRequest(ownerApiKey, repoName))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(
                    ownerApiKey,
                    repoName,
                    entryKey1,
                    OBJECT_MAPPER
                        .createObjectNode()
                        .put("instrumentId", "XAG")
                        .put("name", "Silver")
                        .put("DecimalPrecision", 4)
                        .put("Rounding", "down"))))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(
                    ownerApiKey,
                    repoName,
                    entryKey2,
                    OBJECT_MAPPER
                        .createObjectNode()
                        .put("instrumentId", "JPY")
                        .put("name", "Yen")
                        .put("DecimalPrecision", 2)
                        .put("Rounding", "down"))))
        .block(TIMEOUT);


    configurationService
        .readEntry(new ReadEntryRequest(ownerApiKey, repoName, entryKey1))
        .subscribe(e -> System.out.println(e));

    configurationService
        .readEntry(new ReadEntryRequest(ownerApiKey, repoName, entryKey2))
        .subscribe(e -> System.out.println(e));

        configurationService.readList(new ReadListRequest(ownerApiKey, repoName))
            .subscribe(e -> System.out.println(e));

    try {
      Thread.currentThread().join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

//    StepVerifier.create(configurationService.readList(new ReadListRequest(ownerApiKey, repoName)))
//        .assertNext(
//            entries -> {
//              assertEquals(2, entries.size(), "Fetched entries count");
//
//              List<String> entriesKeys =
//                  entries.stream().map(ReadEntryResponse::key).collect(Collectors.toList());
//
//              assertTrue(
//                  entriesKeys.contains(entryKey1), "Entry " + entryKey1 + " found in response");
//              assertTrue(
//                  entriesKeys.contains(entryKey2), "Entry " + entryKey2 + " found in response");
//            })
//        .expectComplete()
//        .verify();
//
//    StepVerifier.create(configurationService.readList(new ReadListRequest(adminApiKey, repoName)))
//        .assertNext(
//            entries -> {
//              assertEquals(2, entries.size(), "Fetched entries count");
//
//              List<String> entriesKeys =
//                  entries.stream().map(ReadEntryResponse::key).collect(Collectors.toList());
//
//              assertTrue(
//                  entriesKeys.contains(entryKey1), "Entry " + entryKey1 + " found in response");
//              assertTrue(
//                  entriesKeys.contains(entryKey2), "Entry " + entryKey2 + " found in response");
//            })
//        .expectComplete()
//        .verify();
//
//    StepVerifier.create(configurationService.readList(new ReadListRequest(memberToken, repoName)))
//        .assertNext(
//            entries -> {
//              assertEquals(2, entries.size(), "Fetched entries count");
//
//              List<String> entriesKeys =
//                  entries.stream().map(ReadEntryResponse::key).collect(Collectors.toList());
//
//              assertTrue(
//                  entriesKeys.contains(entryKey1), "Entry " + entryKey1 + " found in response");
//              assertTrue(
//                  entriesKeys.contains(entryKey2), "Entry " + entryKey2 + " found in response");
//            })
//        .expectComplete()
//        .verify();
  }
//
//  @TestTemplate
//  @DisplayName(
//      "#27 Fail to get any entry from the non-existent Repository applying some of the accessible API keys: \"Owner\", \"Admin\", \"Member\"")
//  void readEntriesFromNonExistentRepository(
//      ConfigurationService configurationService, OrganizationService organizationService) {
//    String orgId = createOrganization(organizationService).id();
//    String token = createApiKey(organizationService, orgId, Role.Admin).key();
//
//    String repoName = "NON_EXISTENT_REPO";
//
//    StepVerifier.create(configurationService.readList(new ReadListRequest(token, repoName)))
//        .expectErrorMessage(String.format("Repository '%s' not found", repoName))
//        .verify();
//  }
//
//  @TestTemplate
//  @DisplayName(
//      "#28 Fail to get any entry from the Repository upon the \"apiKey\" is invalid (expired)")
//  void readEntriesUsingExpiredToken(
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
//      "#29 Fail to get any entry from the Repository upon the Owner deleted the Organization with related \"Member\" API key")
//  void readEntriesForDeletedOrganization(
//      ConfigurationService configurationService, OrganizationService organizationService)
//      throws InterruptedException {
//    String orgId = createOrganization(organizationService).id();
//    String ownerApiKey = createApiKey(organizationService, orgId, Role.Owner).key();
//    String memberToken = createApiKey(organizationService, orgId, Role.Member).key();
//
//    String repoName = RandomStringUtils.randomAlphabetic(5);
//    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";
//
//    configurationService
//        .createRepository(new CreateRepositoryRequest(ownerApiKey, repoName))
//        .then(
//            configurationService.createEntry(
//                new CreateOrUpdateEntryRequest(
//                    ownerApiKey,
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
//    StepVerifier.create(configurationService.readList(new ReadListRequest(memberToken, repoName)))
//        .expectErrorMessage("Token verification failed")
//        .verify();
//  }
//
//  @TestTemplate
//  @DisplayName(
//      "#30 Fail to get any entry from the Repository upon the Owner applied some of the API keys from another Organization")
//  void readEntriesUsingTokenOfAnotherOrganization(
//      ConfigurationService configurationService, OrganizationService organizationService) {
//    String orgId1 = createOrganization(organizationService).id();
//    String token1 = createApiKey(organizationService, orgId1, Role.Owner).key();
//
//    String orgId2 = createOrganization(organizationService).id();
//    String token2 = createApiKey(organizationService, orgId2, Role.Admin).key();
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
//      "#31 Fail to get any entry from the Repository upon the Member \"apiKey\" (API key) was deleted from the Organization")
//  void readEntriesUsingDeletedToken(
//      ConfigurationService configurationService, OrganizationService organizationService)
//      throws InterruptedException {
//    String orgId = createOrganization(organizationService).id();
//    ApiKey ownerApiKey = createApiKey(organizationService, orgId, Role.Owner);
//    ApiKey memberToken = createApiKey(organizationService, orgId, Role.Member);
//
//    String repoName = RandomStringUtils.randomAlphabetic(5);
//    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";
//
//    configurationService
//        .createRepository(new CreateRepositoryRequest(ownerApiKey.key(), repoName))
//        .then(
//            configurationService.createEntry(
//                new CreateOrUpdateEntryRequest(
//                    ownerApiKey.key(),
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
//            new DeleteOrganizationApiKeyRequest(AUTH0_TOKEN, orgId, memberToken.name()))
//        .block(TIMEOUT);
//
//    TimeUnit.SECONDS.sleep(KEY_CACHE_TTL + 1);
//
//    StepVerifier.create(
//            configurationService.readList(new ReadListRequest(memberToken.key(), repoName)))
//        .expectErrorMessage("Token verification failed")
//        .verify();
//  }
}
