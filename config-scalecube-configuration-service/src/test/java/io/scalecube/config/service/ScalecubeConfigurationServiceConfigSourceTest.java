package io.scalecube.config.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.POJONode;
import io.scalecube.config.ConfigRegistry;
import io.scalecube.config.ObjectConfigProperty;
import io.scalecube.configuration.api.Acknowledgment;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.DeleteEntryRequest;
import io.scalecube.configuration.api.ReadEntryRequest;
import io.scalecube.configuration.api.ReadEntryResponse;
import io.scalecube.configuration.api.CreateOrUpdateEntryRequest;
import io.scalecube.test.fixtures.Fixtures;
import io.scalecube.test.fixtures.WithFixture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@WithFixture(LocalMockServiceFixture.class)
@WithFixture(ProductionServiceFixture.class)
@ExtendWith(Fixtures.class)
class ScalecubeConfigurationServiceConfigSourceTest {

  @TestTemplate
  public void testSingleValue(ConfigRegistry configRegistry, ConfigurationService service)
      throws InterruptedException {

    CountDownLatch latch = new CountDownLatch(1);
    String documentKey = "MyKey1";
    String token = configRegistry.stringValue("token", "token");
    String repository = configRegistry.stringValue("repository", "repository");

    service
        .deleteEntry(new DeleteEntryRequest(token, repository, documentKey))
        .onErrorReturn(new Acknowledgment()) // delete only if needed.
        .block();
    Mono<ReadEntryResponse> fetch = service.readEntry(new ReadEntryRequest(token, repository, documentKey));
    StepVerifier.create(fetch).expectError();

    TimeUnit.SECONDS.sleep(2); // wait for the property to be empty

    ObjectConfigProperty<BrokerData> configProperty =
        configRegistry.objectProperty(
            documentKey, ObjectMapperHolder.parseJsonAs(BrokerData.class));
    configProperty.addCallback(this.onNewValue(latch));
    BrokerData expected =
        new BrokerData(
            "AZSXDC",
            new ApiKey[] {
              new ApiKey(
                  "ASDF", new String[] {"+ALLOW-READ=ALL", "+ALLOW-WRITE=(INSTRUMENT@123345)"}),
              new ApiKey(
                  "QWER", new String[] {"+ALLOW-READ=(INSTRUMENT@123345)", "+ALLOW-WRITE=NONE"})
            });
    JsonNode value = new POJONode(expected);
    service.createEntry(new CreateOrUpdateEntryRequest(token, repository, documentKey, value)).block();
    assertTrue(latch.await(10, TimeUnit.SECONDS), "Time out waiting for a new value");

    assertTrue(configProperty.value().isPresent());
    BrokerData actual = configProperty.value().get();
    assertAll(
        () -> assertEquals(expected.getBrokerID(), actual.getBrokerID(), "Broker ID"),
        () -> assertArrayEquals(expected.getApiKeys(), actual.getApiKeys(), "API keys"),
        () -> assertEquals(expected, actual, "Broker Data"));
  }

  @TestTemplate
  public void testChangeValue(ConfigRegistry configRegistry, ConfigurationService service)
      throws InterruptedException {

    CountDownLatch latchForFirst = new CountDownLatch(1);
    CountDownLatch latchForSecond = new CountDownLatch(2);

    String documentKey = "MyKey2";
    String token = configRegistry.stringValue("token", "token");
    String repository = configRegistry.stringValue("repository", "repository");

    service
        .deleteEntry(new DeleteEntryRequest(token, repository, documentKey))
        .onErrorReturn(new Acknowledgment()) // delete only if needed.
        .block();
    Mono<ReadEntryResponse> fetch = service.readEntry(new ReadEntryRequest(token, repository, documentKey));
    StepVerifier.create(fetch).expectError();
    TimeUnit.SECONDS.sleep(2); // wait for the property to be empty

    ObjectConfigProperty<BrokerData> configProperty =
        configRegistry.objectProperty(
            documentKey, ObjectMapperHolder.parseJsonAs(BrokerData.class));

    configProperty.addCallback(this.onNewValue(latchForFirst, latchForSecond));

    BrokerData expected = new BrokerData("IOPHJK", new ApiKey[] {});
    JsonNode value = new POJONode(expected);
    service.createEntry(new CreateOrUpdateEntryRequest(token, repository, documentKey, value)).block();
    assertTrue(latchForFirst.await(10, TimeUnit.SECONDS), "Time out waiting for a new value");
    assertTrue(configProperty.value().isPresent());
    expected = new BrokerData("QAWSED", new ApiKey[] {});
    value = new POJONode(expected);
    service
        .createEntry(
            new CreateOrUpdateEntryRequest(
                configRegistry.stringValue("token", "token"),
                configRegistry.stringValue("repository", "repository"),
                documentKey,
                value))
        .block();
    assertTrue(latchForSecond.await(10, TimeUnit.SECONDS), "Time out waiting for a new value");
    assertTrue(configProperty.value().isPresent());
    BrokerData actual = configProperty.value().get();
    assertEquals(expected, actual, "Broker ID");
  }

  private BiConsumer<BrokerData, BrokerData> onNewValue(CountDownLatch... latches) {
    return (BrokerData oldValue, BrokerData newValue) -> {
      System.out.println(
          "ScalecubeConfigurationServiceConfigSourceTest.onNewValue():" + newValue.toString());
      for (CountDownLatch latch : latches) {
        latch.countDown();
      }
    };
  }
}
