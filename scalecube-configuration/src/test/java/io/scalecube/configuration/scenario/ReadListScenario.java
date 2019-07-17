package io.scalecube.configuration.scenario;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Role;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.CreateOrUpdateEntryRequest;
import io.scalecube.configuration.api.CreateRepositoryRequest;
import io.scalecube.configuration.api.ReadListRequest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;
import reactor.test.StepVerifier;

/**
 * Todo: need to be implemented. Issue: readList is coming empty but it should have elements. If we
 * make breakpoint on code it coming not empty.
 */
public class ReadListScenario extends BaseScenario {

  @Disabled
  @TestTemplate
  @DisplayName(
      "#53 Scenario: Successful readList (latest key versions) from the related Repository")
  void readEntries(
      ConfigurationService configurationService, OrganizationService organizationService) {
    String orgId = createOrganization(organizationService).id();
    String ownerApiKey = createApiKey(organizationService, orgId, Role.Owner).key();

    String repoName = RandomStringUtils.randomAlphabetic(5);

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
        .createRepository(new CreateRepositoryRequest(ownerApiKey, repoName))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName, entryKey1, entryValue1)))
        .then(
            configurationService.createEntry(
                new CreateOrUpdateEntryRequest(ownerApiKey, repoName, entryKey2, entryValue2)))
        .block(TIMEOUT);

    StepVerifier.create(configurationService.readList(new ReadListRequest(ownerApiKey, repoName)))
        .assertNext(
            entries -> {
              assertEquals(2, entries.size());
            })
        .expectComplete()
        .verify();
  }
}
