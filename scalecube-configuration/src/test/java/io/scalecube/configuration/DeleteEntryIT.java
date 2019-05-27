package io.scalecube.configuration;

import io.scalecube.account.api.OrganizationService;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.fixtures.ContainersConfigurationServiceFixture;
import io.scalecube.configuration.scenario.DeleteEntryScenario;
import io.scalecube.test.fixtures.WithFixture;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@WithFixture(value = ContainersConfigurationServiceFixture.class, lifecycle = Lifecycle.PER_CLASS)
public class DeleteEntryIT extends DeleteEntryScenario {

  public DeleteEntryIT() {
    super(new ITInitBase());
  }

  @Disabled
  @Override
  protected void deleteEntryUsingExpiredToken(
      ConfigurationService configurationService, OrganizationService organizationService) {
  }
}
