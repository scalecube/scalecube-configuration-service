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
import io.scalecube.configuration.api.ReadEntryResponse;
import io.scalecube.configuration.api.ReadListRequest;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/** Todo: need to be implemented in IT. If to stop a breakpoint on code it coming not empty. */
public class ReadListScenario extends BaseScenario {

  private String orgId;
  private String ownerApiKey;
  private String memberApiKey;
  private String repoName;
  private String repoNameNotExists;

  private String entryKey1;
  private String entryKey2;
  private String entryKey3;

  private ObjectNode entryValue11;
  private ObjectNode entryValue12;

  private ObjectNode entryValue21;
  private ObjectNode entryValue22;
  private ObjectNode entryValue23;

  private ObjectNode entryValue31;

  @BeforeEach
  void init(ConfigurationService configurationService, OrganizationService organizationService) {
    orgId = createOrganization(organizationService).id();
    ownerApiKey = createApiKey(organizationService, orgId, Role.Owner).key();
    memberApiKey = createApiKey(organizationService, orgId, Role.Member).key();

    repoName = RandomStringUtils.randomAlphabetic(5);
    repoNameNotExists = repoName + "_not_exists";

    entryKey1 = "KEY-FOR-PRECIOUS-METAL-123";
    entryKey2 = "KEY-FOR-CURRENCY-999";
    entryKey3 = "anyKey";

    entryValue11 =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "XAG")
            .put("name", "Silver")
            .put("DecimalPrecision", 4)
            .put("Rounding", "down");
    entryValue12 =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "JPY")
            .put("name", "Silver")
            .put("DecimalPrecision", 2)
            .put("Rounding", "down");

    entryValue21 =
        OBJECT_MAPPER
            .createObjectNode()
            .put("instrumentId", "JPY")
            .put("name", "Yen")
            .put("DecimalPrecision", 8)
            .put("Rounding", "down");
    entryValue22 = OBJECT_MAPPER.createObjectNode().put("value", "again go");
    entryValue23 = OBJECT_MAPPER.createObjectNode().put("value", 1);

    entryValue31 =
        OBJECT_MAPPER
            .createObjectNode()
            .put("value", JsonArray.create().add("Go go go!!!").toString());

    configurationService
        .createRepository(new CreateRepositoryRequest(ownerApiKey, repoName))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName, entryKey1, entryValue11)))
        .then(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName, entryKey1, entryValue12)))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName, entryKey2, entryValue21)))
        .then(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName, entryKey2, entryValue22)))
        .then(
            configurationService.updateEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName, entryKey2, entryValue23)))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName, entryKey3, entryValue31)))
        .then(Mono.delay(Duration.ofMillis(KEY_CACHE_TTL * 1100)))
        .block(TIMEOUT);
  }

  @TestTemplate
  @DisplayName(
      "#53 Scenario: Successful readList (latest key versions) from the related Repository")
  void readEntries(ConfigurationService configurationService) {

    StepVerifier.create(configurationService.readList(new ReadListRequest(ownerApiKey, repoName)))
        .assertNext(
            entries -> {
              assertEquals(3, entries.size());
              Map<String, Object> keyValue = keyValueMap(entries);
              keyValue.containsKey(entryKey1);
              keyValue.containsValue(entryValue12);

              keyValue.containsKey(entryKey2);
              keyValue.containsValue(entryValue23);

              keyValue.containsKey(entryKey3);
              keyValue.containsValue(entryValue31);
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#54 Scenario: Successful readList (specific key versions) from the related Repository applying all API keys roles")
  void readEntriesWithSpecificVersions(ConfigurationService configurationService) {

    StepVerifier.create(
            configurationService.readList(new ReadListRequest(ownerApiKey, repoName, 1)))
        .assertNext(
            entries -> {
              assertEquals(3, entries.size());

              Map<String, Object> keyValue = keyValueMap(entries);

              assertTrue(keyValue.containsKey(entryKey1));
              assertTrue(keyValue.containsValue(entryValue11));

              assertTrue(keyValue.containsKey(entryKey2));
              assertTrue(keyValue.containsValue(entryValue21));

              assertTrue(keyValue.containsKey(entryKey3));
              assertTrue(keyValue.containsValue(entryValue31));
            })
        .expectComplete()
        .verify();

    StepVerifier.create(
            configurationService.readList(new ReadListRequest(ownerApiKey, repoName, 2)))
        .assertNext(
            entries -> {
              assertEquals(2, entries.size());

              Map<String, Object> keyValue = keyValueMap(entries);

              assertTrue(keyValue.containsKey(entryKey1));
              assertTrue(keyValue.containsValue(entryValue12));

              assertTrue(keyValue.containsKey(entryKey2));
              assertTrue(keyValue.containsValue(entryValue22));
            })
        .expectComplete()
        .verify();

    StepVerifier.create(
            configurationService.readList(new ReadListRequest(ownerApiKey, repoName, 3)))
        .assertNext(
            entries -> {
              assertEquals(1, entries.size());

              Map<String, Object> keyValue = keyValueMap(entries);

              assertTrue(keyValue.containsKey(entryKey2));
              assertTrue(keyValue.containsValue(entryValue23));
            })
        .expectComplete()
        .verify();

    StepVerifier.create(
            configurationService.readList(new ReadListRequest(ownerApiKey, repoName, null)))
        .assertNext(
            entries -> {
              assertEquals(3, entries.size());

              Map<String, Object> keyValue = keyValueMap(entries);

              assertTrue(keyValue.containsKey(entryKey1));
              assertTrue(keyValue.containsValue(entryValue12));

              assertTrue(keyValue.containsKey(entryKey2));
              assertTrue(keyValue.containsValue(entryValue23));

              assertTrue(keyValue.containsKey(entryKey3));
              assertTrue(keyValue.containsValue(entryValue31));
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#55 Scenario: Successful readList of nothing from the related Repository upon no match was found (specific key version doesn't exist)")
  void readEntriesWithNotExistSpecificVersions(ConfigurationService configurationService) {

    StepVerifier.create(
            configurationService.readList(new ReadListRequest(ownerApiKey, repoName, 99)))
        .assertNext(
            entries -> {
              assertEquals(0, entries.size());
            })
        .expectComplete()
        .verify();
  }

  @TestTemplate
  @DisplayName("#56 (current error - Failed to decode data on message q=/configuration/readList)")
  void readEntriesWithNotPositiveIntegerVersion(ConfigurationService configurationService) {
    StepVerifier.create(
            configurationService.readList(new ReadListRequest(ownerApiKey, repoName, "abfdfd")))
        .expectErrorMessage(VERSION_MUST_BE_A_POSITIVE_NUMBER)
        .verify();

    StepVerifier.create(
            configurationService.readList(new ReadListRequest(ownerApiKey, repoName, 0)))
        .expectErrorMessage(VERSION_MUST_BE_A_POSITIVE_NUMBER)
        .verify();

    StepVerifier.create(
            configurationService.readList(new ReadListRequest(ownerApiKey, repoName, -5)))
        .expectErrorMessage(VERSION_MUST_BE_A_POSITIVE_NUMBER)
        .verify();
  }

  @TestTemplate
  @DisplayName("#57 Scenario: Fail to readList due to specified Repository doesn't exist")
  void readEntriesWithNotExistsRepo(ConfigurationService configurationService) {
    StepVerifier.create(
            configurationService.readList(new ReadListRequest(ownerApiKey, repoNameNotExists, 1)))
        .expectErrorMessage(String.format(REPOSITORY_NOT_FOUND_FORMATTER, repoNameNotExists))
        .verify();

    StepVerifier.create(
            configurationService.readList(new ReadListRequest(ownerApiKey, repoNameNotExists)))
        .expectErrorMessage(String.format(REPOSITORY_NOT_FOUND_FORMATTER, repoNameNotExists))
        .verify();

    StepVerifier.create(
            configurationService.readList(new ReadListRequest(ownerApiKey, repoNameNotExists, 1)))
        .expectErrorMessage(String.format(REPOSITORY_NOT_FOUND_FORMATTER, repoNameNotExists))
        .verify();

    StepVerifier.create(
            configurationService.readList(new ReadListRequest(ownerApiKey, repoNameNotExists)))
        .expectErrorMessage(String.format(REPOSITORY_NOT_FOUND_FORMATTER, repoNameNotExists))
        .verify();
  }

  @TestTemplate
  @DisplayName("#58 Scenario: Fail to readList upon the Owner deleted the \"Organization\"")
  void readEntriesWithDeletedOrganization(
      ConfigurationService configurationService, OrganizationService organizationService)
      throws InterruptedException {

    organizationService
        .deleteOrganization(new DeleteOrganizationRequest(AUTH0_TOKEN, orgId))
        .block(TIMEOUT);

    TimeUnit.SECONDS.sleep(KEY_CACHE_TTL + 1);

    StepVerifier.create(configurationService.readList(new ReadListRequest(ownerApiKey, repoName)))
        .expectErrorMessage(TOKEN_VERIFICATION_FAILED)
        .verify();

    StepVerifier.create(
            configurationService.readList(new ReadListRequest(ownerApiKey, repoName, 1)))
        .expectErrorMessage(TOKEN_VERIFICATION_FAILED)
        .verify();
  }

  @TestTemplate
  @DisplayName(
      "#59 Scenario: Fail to readList upon the \"Admin\" apiKey was deleted from the Organization")
  void readEntriesWithDeletedAdminApiKey(
      ConfigurationService configurationService, OrganizationService organizationService)
      throws InterruptedException {

    ApiKey adminApiKey = createApiKey(organizationService, orgId, Role.Admin);

    organizationService
        .deleteOrganizationApiKey(
            new DeleteOrganizationApiKeyRequest(AUTH0_TOKEN, orgId, adminApiKey.name()))
        .block(TIMEOUT);

    TimeUnit.SECONDS.sleep(KEY_CACHE_TTL + 1);

    StepVerifier.create(configurationService.readList(new ReadListRequest(adminApiKey, repoName)))
        .expectErrorMessage(TOKEN_VERIFICATION_FAILED)
        .verify();

    StepVerifier.create(
            configurationService.readList(new ReadListRequest(adminApiKey, repoName, 1)))
        .expectErrorMessage(TOKEN_VERIFICATION_FAILED)
        .verify();
  }

  @TestTemplate
  @DisplayName("#60 Scenario: Fail to readList due to invalid apiKey was applied")
  void readEntryUsingExpiredApiKey(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String apiKey = getExpiredApiKey(organizationService, orgId, Role.Owner).key();

    StepVerifier.create(configurationService.readList(new ReadListRequest(apiKey, repoName)))
        .expectErrorMessage(TOKEN_VERIFICATION_FAILED)
        .verify();

    StepVerifier.create(configurationService.readList(new ReadListRequest(apiKey, repoName, 1)))
        .expectErrorMessage(TOKEN_VERIFICATION_FAILED)
        .verify();
  }

  @TestTemplate
  @DisplayName("#61 Scenario: Fail to readList with empty or undefined apiKey")
  void readEntriesWithEmptyOrUndefinedApiKey(ConfigurationService configurationService) {
    StepVerifier.create(configurationService.readList(new ReadListRequest("", repoName, 1)))
        .expectErrorMessage(PLEASE_SPECIFY_API_KEY)
        .verify();

    StepVerifier.create(configurationService.readList(new ReadListRequest("", repoName)))
        .expectErrorMessage(PLEASE_SPECIFY_API_KEY)
        .verify();

    StepVerifier.create(configurationService.readList(new ReadListRequest(null, repoName, 1)))
        .expectErrorMessage(PLEASE_SPECIFY_API_KEY)
        .verify();

    StepVerifier.create(configurationService.readList(new ReadListRequest(null, repoName)))
        .expectErrorMessage(PLEASE_SPECIFY_API_KEY)
        .verify();
  }

  @TestTemplate
  @DisplayName("#62 Scenario: Fail to readList with empty or undefined Repository name")
  void readEntriesWithEmptyOrUndefinedRepo(ConfigurationService configurationService) {
    StepVerifier.create(configurationService.readList(new ReadListRequest(memberApiKey, "", 1)))
        .expectErrorMessage(PLEASE_SPECIFY_REPO)
        .verify();

    StepVerifier.create(configurationService.readList(new ReadListRequest(memberApiKey, "")))
        .expectErrorMessage(PLEASE_SPECIFY_REPO)
        .verify();

    StepVerifier.create(configurationService.readList(new ReadListRequest(memberApiKey, null, 1)))
        .expectErrorMessage(PLEASE_SPECIFY_REPO)
        .verify();

    StepVerifier.create(configurationService.readList(new ReadListRequest(memberApiKey, null)))
        .expectErrorMessage(PLEASE_SPECIFY_REPO)
        .verify();
  }

  private Map<String, Object> keyValueMap(List<ReadEntryResponse> entries) {
    return entries.stream().collect(Collectors.toMap(rs -> rs.key(), rs -> entryValue(rs.value())));
  }

  private ObjectNode entryValue(Object value) {
    if (value instanceof ObjectNode) {
      return (ObjectNode) value;
    }

    ObjectNode createdValue = OBJECT_MAPPER.createObjectNode();
    ((Map<String, Object>) value)
        .forEach(
            (k, v) -> {
              if (v instanceof String) {
                createdValue.put(k, (String) v);
              } else {
                createdValue.put(k, (Integer) v);
              }
            });
    return createdValue;
  }
}
