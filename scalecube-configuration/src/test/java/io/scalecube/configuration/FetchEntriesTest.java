package io.scalecube.configuration;

import io.scalecube.configuration.fixtures.InMemoryConfigurationServiceFixture;
import io.scalecube.configuration.scenario.DeleteEntryScenario;
import io.scalecube.configuration.scenario.FetchEntriesScenario;
import io.scalecube.test.fixtures.WithFixture;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@WithFixture(value = InMemoryConfigurationServiceFixture.class, lifecycle = Lifecycle.PER_METHOD)
public class FetchEntriesTest extends FetchEntriesScenario {

}
