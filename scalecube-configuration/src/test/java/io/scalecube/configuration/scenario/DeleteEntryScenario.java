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
import io.scalecube.configuration.api.DeleteRequest;
import io.scalecube.configuration.api.FetchRequest;
import io.scalecube.configuration.api.InvalidAuthenticationToken;
import io.scalecube.configuration.api.SaveRequest;
import io.scalecube.configuration.fixtures.InMemoryConfigurationServiceFixture;
import io.scalecube.configuration.repository.exception.KeyNotFoundException;
import io.scalecube.configuration.repository.exception.RepositoryNotFoundException;
import io.scalecube.test.fixtures.Fixtures;
import io.scalecube.test.fixtures.WithFixture;
import java.security.AccessControlException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import reactor.test.StepVerifier;

@ExtendWith(Fixtures.class)
public class DeleteEntryScenario extends BaseScenario {

  @TestTemplate
  @DisplayName(
      "#32 Successful delete of the specific entry from the related Repository applying managers' API keys: \"Owner\" and \"Admin\"")
  void deleteEntry(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = getOrganization(organizationService, ORGANIZATION_1).id();
    String ownerToken = getApiKey(organizationService, orgId, Role.Owner).key();
    String adminToken = getApiKey(organizationService, orgId, Role.Admin).key();

    String repoName = "test-repo";
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

    StepVerifier.create(
            configurationService
                .delete(new DeleteRequest(ownerToken, repoName, entryKey1))
                .then(
                    configurationService.fetch(new FetchRequest(ownerToken, repoName, entryKey1))))
        .expectErrorSatisfies(
            e -> {
              assertEquals(KeyNotFoundException.class, e.getClass());
              assertEquals(String.format("Key '%s' not found", entryKey1), e.getMessage());
            })
        .verify();

    StepVerifier.create(
            configurationService
                .delete(new DeleteRequest(adminToken, repoName, entryKey2))
                .then(
                    configurationService.fetch(new FetchRequest(adminToken, repoName, entryKey2))))
        .expectErrorSatisfies(
            e -> {
              assertEquals(KeyNotFoundException.class, e.getClass());
              assertEquals(String.format("Key '%s' not found", entryKey2), e.getMessage());
            })
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#33 Successful delete one of the identical keys (entries) from the related Repository applying some of the managers' API keys")
  void deleteEntryWithIdenticalKey(
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
        .then(configurationService.save(new SaveRequest(token, repoName2, entryKey, entryValue2)))
        .block(TIMEOUT);

    StepVerifier.create(
            configurationService
                .delete(new DeleteRequest(token, repoName1, entryKey))
                .then(configurationService.fetch(new FetchRequest(token, repoName1, entryKey))))
        .expectErrorSatisfies(
            e -> {
              assertEquals(KeyNotFoundException.class, e.getClass());
              assertEquals(String.format("Key '%s' not found", entryKey), e.getMessage());
            })
        .verify();

    StepVerifier.create(configurationService.fetch(new FetchRequest(token, repoName2, entryKey)))
        .assertNext(
            entry -> {
              assertEquals(entryKey, entry.key(), "Entry key in " + repoName2);
              assertEquals(entryValue2, entry.value(), "Entry value in " + repoName2);
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#34 Fail to delete a specific entry upon the restricted permission due to applying the \"Member\" API key")
  void deleteEntryByMember(
      ConfigurationService configurationService, OrganizationService organizationService) {
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

    StepVerifier.create(
            configurationService
                .delete(new DeleteRequest(memberToken, repoName, entryKey))
                .then(
                    configurationService.fetch(new FetchRequest(memberToken, repoName, entryKey))))
        .expectErrorSatisfies(
            e -> {
              assertEquals(AccessControlException.class, e.getClass());
              assertEquals("Permission denied", e.getMessage());
            })
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#35 Fail to delete a non-existent entry from the related Repository applying the \"Admin\" API key")
  void deleteNonExistingEntryByAdmin(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = getOrganization(organizationService, ORGANIZATION_1).id();
    String ownerToken = getApiKey(organizationService, orgId, Role.Owner).key();
    String adminToken = getApiKey(organizationService, orgId, Role.Admin).key();

    String repoName = "test-repo";
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";
    String nonExistingEntryKey = "NON_EXISTING_KEY";

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

    StepVerifier.create(
            configurationService
                .delete(new DeleteRequest(adminToken, repoName, nonExistingEntryKey))
                .then(configurationService.fetch(new FetchRequest(adminToken, repoName, entryKey))))
        .expectErrorSatisfies(
            e -> {
              assertEquals(KeyNotFoundException.class, e.getClass());
              assertEquals(
                  String.format("Key '%s' not found", nonExistingEntryKey), e.getMessage());
            })
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#36 Fail to delete specific entry from the Repository upon the \"token\" is invalid (expired)")
  void deleteEntryUsingExpiredToken(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = getOrganization(organizationService, ORGANIZATION_1).id();
    String token = getExpiredApiKey(organizationService, orgId, Role.Owner).key();

    StepVerifier.create(configurationService.delete(new DeleteRequest(token, "test-repo", "key")))
        .expectErrorSatisfies(
            e -> {
              assertEquals(InvalidAuthenticationToken.class, e.getClass());
              assertEquals("Token verification failed", e.getMessage());
            })
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#37 Fail to delete specific entry from the Repository upon the Owner deleted the Organization with related \"Owner\" API key")
  void deleteEntryForDeletedOrganization(
      ConfigurationService configurationService, OrganizationService organizationService)
      throws InterruptedException {
    String orgId = getOrganization(organizationService, ORGANIZATION_1).id();
    String token = getApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = "test-repo";
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    configurationService
        .createRepository(new CreateRepositoryRequest(token, repoName))
        .then(
            configurationService.save(
                new SaveRequest(
                    token,
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

    StepVerifier.create(configurationService.delete(new DeleteRequest(token, repoName, entryKey)))
        .expectErrorSatisfies(
            e -> {
              assertEquals(InvalidAuthenticationToken.class, e.getClass());
              assertEquals("Token verification failed", e.getMessage());
            })
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#38 Fail to delete specific entry from the Repository upon the Owner applied some of the API keys from another Organization")
  void deleteEntryUsingTokenOfAnotherOrganization(
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

    StepVerifier.create(configurationService.delete(new DeleteRequest(token2, repoName, entryKey)))
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
      "#39 Fail to delete specific entry from the Repository upon the Owner \"token\" (API key) was deleted from the Organization")
  void deleteEntryUsingDeletedToken(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = getOrganization(organizationService, ORGANIZATION_1).id();
    ApiKey token = getApiKey(organizationService, orgId, Role.Owner);

    String repoName = "test-repo";
    String entryKey = "KEY-FOR-PRECIOUS-METAL-123";

    configurationService
        .createRepository(new CreateRepositoryRequest(token, repoName))
        .then(
            configurationService.save(
                new SaveRequest(
                    token,
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
            new DeleteOrganizationApiKeyRequest(AUTH0_TOKEN, orgId, token.name()))
        .block(TIMEOUT);

    StepVerifier.create(configurationService.delete(new DeleteRequest(token, repoName, entryKey)))
        .expectErrorSatisfies(
            e -> {
              assertEquals(InvalidAuthenticationToken.class, e.getClass());
              assertEquals("Token verification failed", e.getMessage());
            })
        .verify();
  }
}
