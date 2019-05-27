package io.scalecube.configuration.fixtures;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.cluster.DefaultBucketSettings;
import com.couchbase.client.java.cluster.UserRole;
import com.couchbase.client.java.cluster.UserSettings;
import com.github.dockerjava.api.model.PortBinding;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Token;
import io.scalecube.app.decoration.Logo;
import io.scalecube.app.packages.PackageInfo;
import io.scalecube.config.ConfigProperty;
import io.scalecube.config.ConfigRegistry;
import io.scalecube.config.ConfigRegistrySettings;
import io.scalecube.config.source.LoadedConfigProperty;
import io.scalecube.configuration.AppConfiguration;
import io.scalecube.configuration.ConfigurationServiceImpl;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.authorization.DefaultPermissions;
import io.scalecube.configuration.repository.ConfigurationRepository;
import io.scalecube.configuration.repository.couchbase.CouchbaseRepository;
import io.scalecube.configuration.server.DiscoveryOptions;
import io.scalecube.configuration.tokens.OrganizationServiceKeyProvider;
import io.scalecube.organization.OrganizationServiceImpl;
import io.scalecube.organization.repository.couchbase.CouchbaseRepositoryFactory;
import io.scalecube.organization.repository.couchbase.CouchbaseSettings;
import io.scalecube.organization.tokens.InvalidTokenException;
import io.scalecube.organization.tokens.TokenVerifier;
import io.scalecube.organization.tokens.store.KeyStore;
import io.scalecube.organization.tokens.store.VaultKeyStore;
import io.scalecube.security.acl.DefaultAccessControl;
import io.scalecube.security.api.AccessControl;
import io.scalecube.security.api.Authenticator;
import io.scalecube.security.api.Profile;
import io.scalecube.security.jwt.DefaultJwtAuthenticator;
import io.scalecube.services.Microservices;
import io.scalecube.services.ServiceInfo;
import io.scalecube.services.ServiceProvider;
import io.scalecube.services.discovery.ScalecubeServiceDiscovery;
import io.scalecube.services.gateway.clientsdk.Client;
import io.scalecube.services.gateway.clientsdk.ClientSettings;
import io.scalecube.services.transport.rsocket.RSocketServiceTransport;
import io.scalecube.services.transport.rsocket.RSocketTransportResources;
import io.scalecube.test.fixtures.Fixture;
import io.scalecube.transport.Address;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.opentest4j.TestAbortedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.couchbase.CouchbaseContainer;
import org.testcontainers.vault.VaultContainer;
import reactor.core.publisher.Mono;
import reactor.netty.resources.LoopResources;

public class ContainersConfigurationServiceFixture implements Fixture {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(ContainersConfigurationServiceFixture.class);

  private static final String COUCHBASE_DOCKER_IMAGE = "couchbase:community-6.0.0";
  private static final String COUCHBASE_USERNAME = "admin";
  private static final String COUCHBASE_PASSWORD = "123456";
  private static final String COUCHBASE_NETWORK_ALIAS = "couchbase";
  private static final int COUCHBASE_PORT = 8091;

  private static final String VAULT_DOCKER_IMAGE = "vault:0.9.5";
  private static final int VAULT_PORT = 8200;
  private static final String VAULT_TOKEN = "token_for_benchmarks";
  private static final String VAULT_SECRETS_PATH = "secret/configuration-service/dev";
  private static final String VAULT_NETWORK_ALIAS = "vault";
  private static final String VAULT_ADDR_PATTERN = "http://%s:%d";

  private static final int WS_GATEWAY_PORT = 7070;
  private static final int HTTP_GATEWAY_PORT = 8080;
  private static final int RS_GATEWAY_PORT = 9090;
  private static final String GATEWAY_NETWORK_ALIAS = "gateway";

  private static final String BUCKET_FULL_ACCESS = "bucket_full_access";
  public static final String ORGANIZATIONS_BUCKET = "organizations";
  public static final String CONFIGURATIONS_BUCKET = "configurations";

  private Client client;

  private ConfigurationService configurationService;
  private OrganizationService organizationService;

  private CouchbaseContainer couchbaseContainer;
  private VaultContainer vaultContainer;
  private GenericContainer gatewayContainer;
  private GenericContainer organizationServiceContainer;
  private GenericContainer configurationServiceContainer;

  private String vaultIP;
  private String couchbaseIP;
  private String gatewayIP;

  public static void main(String[] args) {
    new ContainersConfigurationServiceFixture().setUp();
  }

  @Override
  public void setUp() throws TestAbortedException {

    setUpCouchbase();
    setUpVault();
    setUpGateway();

    Map<String, String> env = new HashMap<>();
    env.put("VAULT_ADDR", String.format(VAULT_ADDR_PATTERN, VAULT_NETWORK_ALIAS, VAULT_PORT));
    env.put("VAULT_SECRETS_PATH", VAULT_SECRETS_PATH);
    env.put("VAULT_TOKEN", VAULT_TOKEN);


    System.setProperty("VAULT_ADDR", String.format(VAULT_ADDR_PATTERN, "localhost", VAULT_PORT));
    System.setProperty("VAULT_SECRETS_PATH", VAULT_SECRETS_PATH);
    System.setProperty("VAULT_TOKEN", VAULT_TOKEN);

    setUpOrganizationService(env);
    setUpConfigurationService(env);

    try {
      Thread.currentThread().join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    ClientSettings clientSettings = ClientSettings.builder()
        .loopResources(LoopResources.create("ws" + "-loop")).host("localhost").port(7070).build();

    client = Client.websocket(clientSettings);
    organizationService = client.forService(OrganizationService.class);
    configurationService = client.forService(ConfigurationService.class);
  }

  @Override
  public <T> T proxyFor(Class<? extends T> clazz) {
    if (clazz.isAssignableFrom(ConfigurationService.class)) {
      return clazz.cast(configurationService);
    }

    if (clazz.isAssignableFrom(OrganizationService.class)) {
      return clazz.cast(organizationService);
    }

    if (clazz.isAssignableFrom(Client.class)) {
      return clazz.cast(client);
    }

    throw new IllegalArgumentException("Unexpected type: " + clazz);
  }

  @Override
  public void tearDown() {
    configurationServiceContainer.stop();
    organizationServiceContainer.stop();
    gatewayContainer.stop();
    vaultContainer.stop();
    couchbaseContainer.stop();

    try {
      //Waiting for docker cluster fully down and disappeared before next test
      TimeUnit.SECONDS.sleep(5);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private void setUpCouchbase() {
    couchbaseContainer =
        new CouchbaseContainer(COUCHBASE_DOCKER_IMAGE)
            .withClusterAdmin(COUCHBASE_USERNAME, COUCHBASE_PASSWORD)
            .withExposedPorts(8091)
            .withNetwork(Network.SHARED)
//            .withNetworkMode("host")
            .withNetworkAliases(COUCHBASE_NETWORK_ALIAS)
            .withCreateContainerCmdModifier(
                cmd -> {
                  cmd.withName(COUCHBASE_NETWORK_ALIAS);
                  cmd.withPortBindings(PortBinding.parse(COUCHBASE_PORT + ":" + COUCHBASE_PORT));
                })
    ;
    couchbaseContainer.start();
    couchbaseIP = couchbaseContainer.getContainerIpAddress();
    couchbaseContainer.initCluster();
    try {
      couchbaseContainer.callCouchbaseRestAPI("/settings/indexes", "storageMode=forestdb");
    } catch (IOException e) {
      LOGGER.warn("Couchbase set up issues", e);
    }

    createBucket(couchbaseContainer, ORGANIZATIONS_BUCKET, COUCHBASE_PASSWORD);
    createBucket(couchbaseContainer, CONFIGURATIONS_BUCKET, COUCHBASE_PASSWORD);
  }

  private void setUpVault() {
    vaultContainer =
        new VaultContainer<>(VAULT_DOCKER_IMAGE)
            .withVaultPort(VAULT_PORT)
            .withVaultToken(VAULT_TOKEN)
            .withExposedPorts(8200)
            .withSecretInVault(VAULT_SECRETS_PATH,
                "couchbase.hosts=" + COUCHBASE_NETWORK_ALIAS,
                "couchbase.hosts=" + couchbaseIP,
                "couchbase.username=" + COUCHBASE_USERNAME,
                "couchbase.password=" + COUCHBASE_PASSWORD)
            .withNetwork(Network.SHARED)
//            .withNetworkMode("host")
            .withNetworkAliases(VAULT_NETWORK_ALIAS)
            .withCreateContainerCmdModifier(
                cmd -> {
                  cmd.withName(VAULT_NETWORK_ALIAS);
                  cmd.withPortBindings(PortBinding.parse(VAULT_PORT + ":" + VAULT_PORT));
                })
            .waitingFor(new LogMessageWaitStrategy().withRegEx("^.*Vault server started!.*$"));
    vaultContainer.start();
    vaultIP = vaultContainer.getContainerIpAddress();
  }

  private void setUpGateway() {
    gatewayContainer =
        new GenericContainer<>("scalecube/scalecube-services-gateway-runner:2.5.10")
//            .withExposedPorts(WS_GATEWAY_PORT, HTTP_GATEWAY_PORT, RS_GATEWAY_PORT, 4801)
//            .withNetwork(Network.SHARED)
            .withNetworkMode("host")
//            .withNetworkAliases(GATEWAY_NETWORK_ALIAS)
            .withCreateContainerCmdModifier(
                cmd -> {
                  cmd.withName(GATEWAY_NETWORK_ALIAS);
//                  cmd.withPortBindings(
//                      PortBinding.parse(WS_GATEWAY_PORT + ":" + WS_GATEWAY_PORT),
//                      PortBinding.parse(HTTP_GATEWAY_PORT + ":" + HTTP_GATEWAY_PORT),
//                      PortBinding.parse(RS_GATEWAY_PORT + ":" + RS_GATEWAY_PORT)
//                  );
                })
            .waitingFor(new HostPortWaitStrategy());
    gatewayContainer.start();

    gatewayIP = gatewayContainer.getContainerIpAddress();
  }

  private void setUpConfigurationService(Map<String, String> env) {
//    env.put("JAVA_OPTS", "-Dio.scalecube.configuration.seeds=" + GATEWAY_NETWORK_ALIAS + ":4801 "
//        + "-Dkey.cache.ttl=2 -Dkey.cache.refresh.interval=1");

    Microservices microservices =
        Microservices.builder()

            .discovery(
                (serviceEndpoint) ->
                    new ScalecubeServiceDiscovery(serviceEndpoint)
                        .options(
                            opts ->
                                opts.seedMembers(Address.create(gatewayIP, 4801))
                                    .port(4811)
//                                    .memberHost(discoveryOptions.memberHost())
//                                    .memberPort(discoveryOptions.memberPort())
                        ))
            .transport(
                opts ->
                    opts.resources(RSocketTransportResources::new)
                        .client(RSocketServiceTransport.INSTANCE::clientTransport)
                        .server(RSocketServiceTransport.INSTANCE::serverTransport)
                        .port(4815))
            .services(createConfigurationService())
            .startAwait();

    Logo.from(new PackageInfo())
        .ip(microservices.serviceAddress().host())
        .port(microservices.serviceAddress().port() + "")
        .draw();
  }

  private ServiceProvider createConfigurationService() {

    ConfigurationRepository configurationRepository =
        new CouchbaseRepository(couchbaseBucket());

    return call -> {
      OrganizationService organizationService = call.api(OrganizationService.class);

      OrganizationServiceKeyProvider keyProvider =
          new OrganizationServiceKeyProvider(organizationService);

      Authenticator authenticator =
          new DefaultJwtAuthenticator(map -> keyProvider.get(map.get("kid").toString()).block());

      AccessControl accessControl =
          DefaultAccessControl.builder()
              .authenticator(authenticator)
              .authorizer(DefaultPermissions.PERMISSIONS)
              .build();

      ConfigurationService configurationService =
          new ConfigurationServiceImpl(configurationRepository, accessControl);

      return Collections.singleton(ServiceInfo.fromServiceInstance(configurationService).build());
    };
  }

  private AsyncBucket couchbaseBucket() {
//    return Mono.fromCallable(() -> CouchbaseCluster.create(String.format(VAULT_ADDR_PATTERN, couchbaseIP, COUCHBASE_PORT))
    return Mono.fromCallable(() -> CouchbaseCluster.create(String.format(VAULT_ADDR_PATTERN, "localhost", COUCHBASE_PORT))
        .authenticate(COUCHBASE_USERNAME, COUCHBASE_PASSWORD)
        .openBucket(CONFIGURATIONS_BUCKET)
        .async()).retryBackoff(3, Duration.ofSeconds(1)).block(Duration.ofSeconds(30));
  }

  private void createBucket(CouchbaseContainer couchbase, String name, String password) {
    couchbase.createBucket(
        DefaultBucketSettings.builder().name(name).password(password).build(),
        UserSettings.build()
            .name(name)
            .password(password)
            .roles(Collections.singletonList(new UserRole(BUCKET_FULL_ACCESS, name))),
        true);
  }

  private void setUpOrganizationService(Map<String, String> env) {

    Address seedAddress = Address.create(gatewayIP, 4801);

    Microservices.builder()
        .discovery(
            (serviceEndpoint) ->
                new ScalecubeServiceDiscovery(serviceEndpoint)
                    .options(
                        opts ->
                            opts.seedMembers(seedAddress)
                                .port(4821)
//                                .memberHost(memberHost)

                    ))
        .transport(
            opts ->
                opts.resources(RSocketTransportResources::new)
                    .client(RSocketServiceTransport.INSTANCE::clientTransport)
                    .server(RSocketServiceTransport.INSTANCE::serverTransport)
                    .port(4825))
        .services(createOrganizationService())
        .startAwait();
  }

  private OrganizationService createOrganizationService() {

    CouchbaseSettings couchbaseSettings = couchbaseSettings();

    CouchbaseRepositoryFactory factory = new CouchbaseRepositoryFactory(couchbaseSettings);

    KeyStore keyStore = new VaultKeyStore();

    TokenVerifier tokenVerifier = mockTokenVerifier();

    return new OrganizationServiceImpl(factory.organizations(), keyStore, tokenVerifier);
  }

  private CouchbaseSettings couchbaseSettings() {

    Map<String, String> map = new HashMap<String, String>() {{
      put("couchbase.hosts", String.format(VAULT_ADDR_PATTERN, "localhost", COUCHBASE_PORT));
      put("couchbase.organizationsBucketName", ORGANIZATIONS_BUCKET);
      put("couchbase.username", COUCHBASE_USERNAME);
      put("couchbase.password", COUCHBASE_PASSWORD);
    }};

    Map<String, ConfigProperty> configMap =
        map.entrySet().stream()
            .map(LoadedConfigProperty::withNameAndValue)
            .map(LoadedConfigProperty.Builder::build)
            .collect(Collectors.toMap(LoadedConfigProperty::name, Function.identity()));

    ConfigRegistry configRegistry;

    ConfigRegistrySettings.Builder builder =
        ConfigRegistrySettings.builder();

    builder.addLastSource("couchbaseDB", () -> configMap);

    configRegistry = ConfigRegistry.create(builder.build());

    return configRegistry.objectProperty("couchbase", CouchbaseSettings.class)
        .value()
        .orElseThrow(() -> new IllegalStateException("Couldn't load couchbase settings"));
  }

  private TokenVerifier mockTokenVerifier() {
    return token -> {

      Map claims = new HashMap<>();
      claims.put("aud", "https://anytest.auth0.com/api/v2/");
      claims.put("role", "Owner");

      return Profile.builder()
          .userId("Ex5ToloIfQ7G7fPDJdatZdj4Pn2K36Aw@clients")
          .email("any@testemail.any")
          .email(null)
          .emailVerified(false)
          .name(null)
          .familyName(null)
          .givenName(null)
          .claims(claims).build();
    };
  }
}
