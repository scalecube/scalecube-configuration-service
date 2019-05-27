package io.scalecube.configuration.it;

import io.scalecube.configuration.fixtures.IntegrationEnvironmentFixture;
import io.scalecube.configuration.scenario.SaveEntryScenario;
import io.scalecube.test.fixtures.Fixtures;
import io.scalecube.test.fixtures.WithFixture;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(Fixtures.class)
@WithFixture(value = IntegrationEnvironmentFixture.class, lifecycle = Lifecycle.PER_CLASS)
final class SaveEntryIT extends SaveEntryScenario {}
