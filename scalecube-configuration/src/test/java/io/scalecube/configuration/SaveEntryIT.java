package io.scalecube.configuration;

import io.scalecube.account.api.OrganizationService;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.fixtures.ContainersConfigurationServiceFixture;
import io.scalecube.configuration.scenario.SaveEntryScenario;
import io.scalecube.test.fixtures.WithFixture;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@WithFixture(value = ContainersConfigurationServiceFixture.class, lifecycle = Lifecycle.PER_CLASS)
public class SaveEntryIT extends SaveEntryScenario {

  public SaveEntryIT() {
    super(new ITInitBase());
  }

  @Disabled
  @Override
  protected void saveEntryUsingExpiredToken(
      ConfigurationService configurationService, OrganizationService organizationService) {
  }
}
