package io.scalecube.config.service;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import io.scalecube.config.ConfigRegistry;
import io.scalecube.config.ConfigRegistrySettings;
import io.scalecube.config.source.SystemPropertiesConfigSource;
import io.scalecube.configuration.api.Acknowledgment;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.CreateEntryRequest;
import io.scalecube.configuration.api.DeleteEntryRequest;
import io.scalecube.configuration.api.ReadEntryRequest;
import io.scalecube.configuration.api.ReadEntryResponse;
import io.scalecube.services.exceptions.InternalServiceException;
import io.scalecube.test.fixtures.Fixture;
import java.util.ArrayList;
import java.util.List;
import org.opentest4j.TestAbortedException;
import reactor.core.publisher.Mono;

public class LocalMockServiceFixture implements Fixture {

  private ConfigurationService service;
  private ConfigRegistry configRegistry;

  @Override
  public void setUp() throws TestAbortedException {

    List<ReadEntryResponse> responses = new ArrayList<>();
    Acknowledgment acknowledgment = new Acknowledgment();
    service = mock(ConfigurationService.class);
    when(service.readList(any()))
        .then(
            answer -> {
              return Mono.just(responses);
            });
    when(service.createEntry(any()))
        .then(
            answer -> {
              CreateEntryRequest request = (CreateEntryRequest) answer.getArguments()[0];
              JsonNode value = (JsonNode) request.value();
              ReadEntryResponse response = new ReadEntryResponse(request.key(), value);
              responses.add(response);
              return Mono.just(acknowledgment);
            });
    when(service.deleteEntry(any()))
        .then(
            answer -> {
              DeleteEntryRequest request = (DeleteEntryRequest) answer.getArguments()[0];
              responses.removeIf(response -> request.key().equals(response.key()));
              return Mono.just(acknowledgment);
            });
    when(service.readEntry(any()))
        .then(
            answer -> {
              ReadEntryRequest request = (ReadEntryRequest) answer.getArguments()[0];
              return Mono.just(
                      responses
                          .stream()
                          .filter(response -> request.key().equals(response.key()))
                          .findFirst())
                  .flatMap(
                      o ->
                          o.isPresent()
                              ? Mono.just(o.get())
                              : Mono.error(
                                  () ->
                                      new InternalServiceException(
                                          500, "Key '" + request.key() + "' not found")));
            });

    String token = "42";
    String repository = "cafe-42";

    configRegistry =
        ConfigRegistry.create(
            ConfigRegistrySettings.builder()
                .addFirstSource("System", new SystemPropertiesConfigSource())
                .addLastSource(
                    "ScalecubeConfigurationService",
                    ScalecubeConfigurationServiceConfigSource.builder(service)
                        .token(token)
                        .repository(repository)
                        .build())
                .reloadIntervalSec(1)
                .build());
  }

  @Override
  public <T> T proxyFor(Class<? extends T> clasz) {
    if (clasz.isAssignableFrom(ConfigurationService.class)) {
      return clasz.cast(service);
    } else if (clasz.isAssignableFrom(ConfigRegistry.class)) {
      return clasz.cast(configRegistry);
    }
    return null;
  }

  @Override
  public void tearDown() {
    // nothing to do here
  }
}