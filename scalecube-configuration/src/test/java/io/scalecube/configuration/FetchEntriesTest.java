package io.scalecube.configuration;

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
import io.scalecube.configuration.api.InvalidAuthenticationToken;
import io.scalecube.configuration.api.SaveRequest;
import io.scalecube.configuration.fixtures.InMemoryConfigurationServiceFixture;
import io.scalecube.configuration.repository.exception.RepositoryNotFoundException;
import io.scalecube.test.fixtures.Fixtures;
import io.scalecube.test.fixtures.WithFixture;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import reactor.test.StepVerifier;

@ExtendWith(Fixtures.class)
@WithFixture(value = InMemoryConfigurationServiceFixture.class, lifecycle = Lifecycle.PER_METHOD)
final class FetchEntriesTest extends BaseTest {

  @TestTemplate
  @DisplayName(
      "#26 Successful get of the all existent entries list from the related Repository applying the all related API keys: \"Owner\", \"Admin\", \"Member\"")
  void fetchEntries(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = getOrganization(organizationService, ORGANIZATION_1).id();
    String ownerToken = getApiKey(organizationService, orgId, Role.Owner).key();
    String adminToken = getApiKey(organizationService, orgId, Role.Admin).key();
    String memberToken = getApiKey(organizationService, orgId, Role.Member).key();

    String repoName = "test-repo";
    String entryKey1 = "KEY-FOR-CURRENCY-999";
    String entryKey2 = "KEY-FOR-PRECIOUS-METAL-123";

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
    test26WithEntriesMethod(
        configurationService,
        organizationService,
        repoName,
        ownerToken,
        adminToken,
        memberToken,
        entryKey1,
        entryKey2);
    test26WithFetchAllMethod(
        configurationService,
        organizationService,
        repoName,
        ownerToken,
        adminToken,
        memberToken,
        entryKey1,
        entryKey2);
    ;
  }

  /**
   * @param configurationService
   * @param organizationService
   * @param ownerToken
   * @param adminToken
   * @param memberToken
   * @param entryKey1
   * @throws AssertionError
   */
  private static void test26WithEntriesMethod(
      ConfigurationService configurationService,
      OrganizationService organizationService,
      String repoName,
      Object ownerToken,
      Object adminToken,
      Object memberToken,
      String entryKey1,
      String entryKey2)
      throws AssertionError {
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

  /**
   * @param configurationService
   * @param organizationService
   * @param ownerToken
   * @param adminToken
   * @param memberToken
   * @param entryKey1
   * @throws AssertionError
   */
  private static void test26WithFetchAllMethod(
      ConfigurationService configurationService,
      OrganizationService organizationService,
      String repoName,
      Object ownerToken,
      Object adminToken,
      Object memberToken,
      String entryKey1,
      String entryKey2)
      throws AssertionError {

    StepVerifier.create(
            configurationService
                .fetchAll(new EntriesRequest(ownerToken, repoName))
                .sort(Comparator.comparing(FetchResponse::key)))
        .assertNext(response -> assertEquals(entryKey1, response.key(), "entry 1 exists"))
        .assertNext(response -> assertEquals(entryKey2, response.key(), "entry 2 exists"))
        .expectComplete()
        .verify();

    StepVerifier.create(
            configurationService
                .fetchAll(new EntriesRequest(adminToken, repoName))
                .sort(Comparator.comparing(FetchResponse::key)))
        .assertNext(response -> assertEquals(entryKey1, response.key(), "entry 1 exists"))
        .assertNext(response -> assertEquals(entryKey2, response.key(), "entry 2 exists"))
        .expectComplete()
        .verify();

    StepVerifier.create(
            configurationService
                .fetchAll(new EntriesRequest(memberToken, repoName))
                .sort(Comparator.comparing(FetchResponse::key)))
        .assertNext(response -> assertEquals(entryKey1, response.key(), "entry 1 exists"))
        .assertNext(response -> assertEquals(entryKey2, response.key(), "entry 2 exists"))
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#27 Fail to get any entry from the non-existent Repository applying some of the accessible API keys: \"Owner\", \"Admin\", \"Member\"")
  void fetchEntriesFromNonExistentRepository(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = getOrganization(organizationService, ORGANIZATION_1).id();
    String token = getApiKey(organizationService, orgId, Role.Admin).key();

    String repoName = "NON_EXISTENT_REPO";

    StepVerifier.create(configurationService.entries(new EntriesRequest(token, repoName)))
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
      "#27 Fail to get any entry from the non-existent Repository applying some of the accessible API keys: \"Owner\", \"Admin\", \"Member\"")
  void fetchAllFromNonExistentRepository(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = getOrganization(organizationService, ORGANIZATION_1).id();
    String token = getApiKey(organizationService, orgId, Role.Admin).key();

    String repoName = "NON_EXISTENT_REPO";

    StepVerifier.create(configurationService.fetchAll(new EntriesRequest(token, repoName)))
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
      "#28 Fail to get any entry from the Repository upon the \"token\" is invalid (expired)")
  void fetchEntriesUsingExpiredToken(
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
      "#28 Fail to get any entry from the Repository upon the \"token\" is invalid (expired)")
  void fetchAllUsingExpiredToken(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = getOrganization(organizationService, ORGANIZATION_1).id();
    String token = getExpiredApiKey(organizationService, orgId, Role.Owner).key();

    StepVerifier.create(configurationService.fetchAll(new EntriesRequest(token, "test-repo")))
        .expectErrorSatisfies(
            e -> {
              assertEquals(InvalidAuthenticationToken.class, e.getClass());
              assertEquals("Token verification failed", e.getMessage());
            })
        .verify();
  }
  
  @TestTemplate
  @DisplayName(
      "#29 Fail to get any entry from the Repository upon the Owner deleted the Organization with related \"Member\" API key")
  void fetchEntriesForDeletedOrganization(
      ConfigurationService configurationService, OrganizationService organizationService)
      throws InterruptedException {
    String orgId = getOrganization(organizationService, ORGANIZATION_1).id();
    String ownerToken = getApiKey(organizationService, orgId, Role.Owner).key();
    String memberToken = getApiKey(organizationService, orgId, Role.Member).key();

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

    StepVerifier.create(configurationService.entries(new EntriesRequest(memberToken, repoName)))
        .expectErrorSatisfies(
            e -> {
              assertEquals(InvalidAuthenticationToken.class, e.getClass());
              assertEquals("Token verification failed", e.getMessage());
            })
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#29 Fail to get any entry from the Repository upon the Owner deleted the Organization with related \"Member\" API key")
  void fetchAllForDeletedOrganization(
      ConfigurationService configurationService, OrganizationService organizationService)
      throws InterruptedException {
    String orgId = getOrganization(organizationService, ORGANIZATION_1).id();
    String ownerToken = getApiKey(organizationService, orgId, Role.Owner).key();
    String memberToken = getApiKey(organizationService, orgId, Role.Member).key();

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

    StepVerifier.create(configurationService.fetchAll(new EntriesRequest(memberToken, repoName)))
        .expectErrorSatisfies(
            e -> {
              assertEquals(InvalidAuthenticationToken.class, e.getClass());
              assertEquals("Token verification failed", e.getMessage());
            })
        .verify();
  }
  
  @TestTemplate
  @DisplayName(
      "#30 Fail to get any entry from the Repository upon the Owner applied some of the API keys from another Organization")
  void fetchEntriesUsingTokenOfAnotherOrganization(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId1 = getOrganization(organizationService, ORGANIZATION_1).id();
    String token1 = getApiKey(organizationService, orgId1, Role.Owner).key();

    String orgId2 = getOrganization(organizationService, ORGANIZATION_2).id();
    String token2 = getApiKey(organizationService, orgId2, Role.Admin).key();

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

  @TestTemplate
  @DisplayName(
      "#30 Fail to get any entry from the Repository upon the Owner applied some of the API keys from another Organization")
  void fetchAllUsingTokenOfAnotherOrganization(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId1 = getOrganization(organizationService, ORGANIZATION_1).id();
    String token1 = getApiKey(organizationService, orgId1, Role.Owner).key();

    String orgId2 = getOrganization(organizationService, ORGANIZATION_2).id();
    String token2 = getApiKey(organizationService, orgId2, Role.Admin).key();

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

    StepVerifier.create(configurationService.fetchAll(new EntriesRequest(token2, repoName)))
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
      "#31 Fail to get any entry from the Repository upon the Member \"token\" (API key) was deleted from the Organization")
  void fetchEntriesUsingDeletedToken(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = getOrganization(organizationService, ORGANIZATION_1).id();
    ApiKey ownerToken = getApiKey(organizationService, orgId, Role.Owner);
    ApiKey memberToken = getApiKey(organizationService, orgId, Role.Member);

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
            new DeleteOrganizationApiKeyRequest(AUTH0_TOKEN, orgId, memberToken.name()))
        .block(TIMEOUT);

    StepVerifier.create(configurationService.entries(new EntriesRequest(memberToken, repoName)))
        .expectErrorSatisfies(
            e -> {
              assertEquals(InvalidAuthenticationToken.class, e.getClass());
              assertEquals("Token verification failed", e.getMessage());
            })
        .verify();
  }
  
  @Disabled("Feature is not implemented")
  @TestTemplate
  @DisplayName(
      "#31 Fail to get any entry from the Repository upon the Member \"token\" (API key) was deleted from the Organization")
  void fetchAllUsingDeletedToken(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = getOrganization(organizationService, ORGANIZATION_1).id();
    ApiKey ownerToken = getApiKey(organizationService, orgId, Role.Owner);
    ApiKey memberToken = getApiKey(organizationService, orgId, Role.Member);

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
            new DeleteOrganizationApiKeyRequest(AUTH0_TOKEN, orgId, memberToken.name()))
        .block(TIMEOUT);

    StepVerifier.create(configurationService.fetchAll(new EntriesRequest(memberToken, repoName)))
        .expectErrorSatisfies(
            e -> {
              assertEquals(InvalidAuthenticationToken.class, e.getClass());
              assertEquals("Token verification failed", e.getMessage());
            })
        .verify();
  }
}
