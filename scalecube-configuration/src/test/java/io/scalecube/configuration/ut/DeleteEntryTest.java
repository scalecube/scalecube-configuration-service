package io.scalecube.configuration.ut;

import io.scalecube.configuration.fixtures.InMemoryEnvironmentFixture;
import io.scalecube.configuration.scenario.DeleteEntryScenario;
import io.scalecube.test.fixtures.Fixtures;
import io.scalecube.test.fixtures.WithFixture;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(Fixtures.class)
@WithFixture(value = InMemoryEnvironmentFixture.class, lifecycle = Lifecycle.PER_METHOD)
final class DeleteEntryTest extends DeleteEntryScenario {
}
