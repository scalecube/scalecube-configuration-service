package io.scalecube.configuration.it;

import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.fixtures.IntegrationEnvironmentFixture;
import io.scalecube.configuration.scenario.ReadListScenario;
import io.scalecube.test.fixtures.Fixtures;
import io.scalecube.test.fixtures.WithFixture;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(Fixtures.class)
@WithFixture(value = IntegrationEnvironmentFixture.class, lifecycle = Lifecycle.PER_METHOD)
final class ReadListIT extends ReadListScenario {

  @Override
  protected void readEntries(ConfigurationService configurationService) {
    // not used
  }

  @Override
  protected void readEntriesWithSpecificVersions(ConfigurationService configurationService) {
    // not used
  }
}
