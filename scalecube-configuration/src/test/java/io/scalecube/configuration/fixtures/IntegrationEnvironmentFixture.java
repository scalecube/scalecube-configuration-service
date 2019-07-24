package io.scalecube.configuration.fixtures;

import static io.scalecube.services.gateway.transport.GatewayClientTransports.WEBSOCKET_CLIENT_CODEC;

import io.scalecube.net.Address;
import io.scalecube.services.ServiceCall;
import io.scalecube.services.gateway.transport.GatewayClient;
import io.scalecube.services.gateway.transport.GatewayClientSettings;
import io.scalecube.services.gateway.transport.GatewayClientTransport;
import io.scalecube.services.gateway.transport.StaticAddressRouter;
import io.scalecube.services.gateway.transport.websocket.WebsocketGatewayClient;
import io.scalecube.test.fixtures.Fixture;
import java.time.Duration;
import org.opentest4j.TestAbortedException;

public final class IntegrationEnvironmentFixture implements Fixture {

  private static final IntegrationEnvironment environment = new IntegrationEnvironment();
  private static String HOST = "localhost";
  private static int PORT = 7070;

  private ServiceCall serviceCall;

  static {
    environment.start();
  }

  private GatewayClient client;
  private GatewayClientTransport clientTransport;

  @Override
  public void setUp() throws TestAbortedException {
    GatewayClientSettings settings = GatewayClientSettings.builder().host(HOST).port(PORT).build();

    client = new WebsocketGatewayClient(settings, WEBSOCKET_CLIENT_CODEC);
    clientTransport = new GatewayClientTransport(client);
    serviceCall =
        new ServiceCall()
            .transport(clientTransport)
            .router(new StaticAddressRouter(Address.create(HOST, PORT)));
  }

  @Override
  public <T> T proxyFor(Class<? extends T> clazz) {
    return serviceCall.api(clazz);
  }

  @Override
  public void tearDown() {
    if (client != null) {
      client.close().block(Duration.ofSeconds(10));
    }
  }
}
