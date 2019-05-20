package io.scalecube.configuration;

import io.scalecube.configuration.fixtures.ContainersConfigurationServiceFixture;
import io.scalecube.configuration.scenario.FetchEntryScenario;
import io.scalecube.test.fixtures.WithFixture;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@WithFixture(value = ContainersConfigurationServiceFixture.class, lifecycle = Lifecycle.PER_METHOD)
public class FetchEntryIT extends FetchEntryScenario {

}
