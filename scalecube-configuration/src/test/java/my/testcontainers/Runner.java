package my.testcontainers;

import io.scalecube.app.decoration.Logo;
import io.scalecube.app.packages.PackageInfo;
import io.scalecube.services.Microservices;
import io.scalecube.services.discovery.ScalecubeServiceDiscovery;
import io.scalecube.services.transport.rsocket.RSocketServiceTransport;
import io.scalecube.services.transport.rsocket.RSocketTransportResources;
import io.scalecube.transport.Address;

public class Runner {

  /**
   * Runner.
   *
   * @param args args
   */
  public static void main(String[] args) {

    int discoveryPort = Integer.getInteger("DISCOVERY_PORT", 4801);
    Address seeds = Address.from(System.getProperty("SEEDS", "localhost:" + discoveryPort));
    String memberHost = System.getProperty("MEMBER_HOST", null);
    Integer memberPort = Integer.getInteger("MEMBER_PORT", null);

    Microservices microservices =
        Microservices.builder()
            .discovery(
                (serviceEndpoint) ->
                    new ScalecubeServiceDiscovery(serviceEndpoint)
                        .options(
                            opts ->
                                opts.seedMembers(seeds)
                                    .port(discoveryPort)
                                    .memberHost(memberHost)
                                    .memberPort(memberPort)))
            .transport(
                opts ->
                    opts.resources(RSocketTransportResources::new)
                        .client(RSocketServiceTransport.INSTANCE::clientTransport)
                        .server(RSocketServiceTransport.INSTANCE::serverTransport))
            .startAwait();

    Logo.from(new PackageInfo())
        .ip(microservices.serviceAddress().host())
        .port(String.valueOf(microservices.serviceAddress().port()))
        .draw();

    microservices.onShutdown().block();
  }
}
