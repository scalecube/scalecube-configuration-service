package io.scalecube.configuration.fixtures;

import static io.scalecube.configuration.scenario.BaseScenario.API_KEY_TTL_IN_SECONDS;

import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.cluster.DefaultBucketSettings;
import com.couchbase.client.java.cluster.UserRole;
import com.couchbase.client.java.cluster.UserSettings;
import com.github.dockerjava.api.model.PortBinding;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.config.ConfigRegistry;
import io.scalecube.configuration.ConfigurationServiceImpl;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.authorization.DefaultPermissions;
import io.scalecube.configuration.repository.ConfigurationRepository;
import io.scalecube.configuration.repository.couchbase.CouchbaseRepository;
import io.scalecube.configuration.server.DiscoveryOptions;
import io.scalecube.configuration.tokens.OrganizationServiceKeyProvider;
import io.scalecube.organization.OrganizationServiceImpl;
import io.scalecube.organization.config.AppConfiguration;
import io.scalecube.organization.repository.couchbase.CouchbaseRepositoryFactory;
import io.scalecube.organization.repository.couchbase.CouchbaseSettings;
import io.scalecube.organization.tokens.TokenVerifier;
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
import io.scalecube.services.gateway.ws.WebsocketGateway;
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
import org.opentest4j.TestAbortedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.couchbase.CouchbaseContainer;
import org.testcontainers.vault.VaultContainer;
import reactor.core.publisher.Mono;
import reactor.netty.resources.LoopResources;

public final class IntegrationEnvironmentFixture implements Fixture {

  private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationEnvironmentFixture.class);

  private static final String COUCHBASE_DOCKER_IMAGE = "couchbase:community-6.0.0";
  private static final String COUCHBASE_USERNAME = "admin";
  private static final String COUCHBASE_PASSWORD = "123456";

  private static final String VAULT_DOCKER_IMAGE = "vault:0.9.5";
  private static final int VAULT_PORT = 8200;
  private static final String VAULT_TOKEN = "token_for_benchmarks";
  private static final String VAULT_SECRETS_PATH = "secret/configuration-service/dev";
  private static final String VAULT_ADDR_PATTERN = "http://%s:%d";

  private static final int WS_GATEWAY_PORT = 7070;

  private static final String BUCKET_FULL_ACCESS = "bucket_full_access";

  private CouchbaseContainer couchbase;
  private VaultContainer vault;
  private Microservices gateway;
  private Microservices organizationService;
  private Microservices configurationService;
  private Client client;

  @Override
  public void setUp() throws TestAbortedException {
    LOGGER.info("### Start environment");

    try {
      Map<String, String> env = new HashMap<>();
      env.put("VAULT_ADDR", String.format(VAULT_ADDR_PATTERN, "localhost", VAULT_PORT));
      env.put("VAULT_SECRETS_PATH", VAULT_SECRETS_PATH);
      env.put("VAULT_TOKEN", VAULT_TOKEN);

      setEnv(env);

      couchbase = startCouchbase();
      vault = startVault();
      gateway = startGateway();
      organizationService = startOrganizationService();
      configurationService = startConfigurationService();

      ClientSettings settings =
          ClientSettings.builder()
              .host("localhost")
              .port(7070)
              .loopResources(LoopResources.create("integration-tests-client"))
              .build();

      client = Client.websocket(settings);
    } catch (Exception e) {
      LOGGER.error("### Error on environment set up", e);

      tearDown();

      throw new RuntimeException("Error on environment set up", e);
    }

    LOGGER.info("### Environment is running");
  }

  @Override
  public <T> T proxyFor(Class<? extends T> clazz) {
    return client.forService(clazz);
  }

  @Override
  public void tearDown() {
    LOGGER.info("### Stop environment");

    try {
      if (configurationService != null) {
        configurationService.shutdown().block();
      }
      if (organizationService != null) {
        organizationService.shutdown().block();
      }
      if (gateway != null) {
        gateway.shutdown().block();
      }
      if (vault != null) {
        vault.stop();
      }
      if (couchbase != null) {
        couchbase.stop();
      }

      TimeUnit.SECONDS.sleep(10);
    } catch (Exception e) {
      LOGGER.error("### Error on stopping environment", e);
      throw new RuntimeException("Error on stopping environment", e);
    }

    LOGGER.info("### Environment is stopped");
  }

  private CouchbaseContainer startCouchbase() {
    LOGGER.info("### Start couchbase");

    CouchbaseContainer couchbase =
        new CouchbaseContainer(COUCHBASE_DOCKER_IMAGE)
            .withClusterAdmin(COUCHBASE_USERNAME, COUCHBASE_PASSWORD)
            .withCreateContainerCmdModifier(
                cmd -> cmd.withPortBindings(PortBinding.parse("8091:8091")));
    couchbase.start();
    couchbase.initCluster();
    try {
      couchbase.callCouchbaseRestAPI("/settings/indexes", "storageMode=forestdb");
    } catch (IOException e) {
      // ignore
    }

    String name = "organizations";
    couchbase.createBucket(
        DefaultBucketSettings.builder().name(name).password(COUCHBASE_PASSWORD).build(),
        UserSettings.build()
            .name(name)
            .password(COUCHBASE_PASSWORD)
            .roles(Collections.singletonList(new UserRole(BUCKET_FULL_ACCESS, name))),
        true);

    String configName = "configurations";
    couchbase.createBucket(
        DefaultBucketSettings.builder().name(configName).password(COUCHBASE_PASSWORD).build(),
        UserSettings.build()
            .name(configName)
            .password(COUCHBASE_PASSWORD)
            .roles(Collections.singletonList(new UserRole(BUCKET_FULL_ACCESS, configName))),
        true);

    return couchbase;
  }

  private VaultContainer startVault() {
    LOGGER.info("### Start vault");

    VaultContainer<?> vault =
        new VaultContainer<>(VAULT_DOCKER_IMAGE)
            .withVaultPort(VAULT_PORT)
            .withVaultToken(VAULT_TOKEN)
            .withSecretInVault(
                VAULT_SECRETS_PATH,
                "couchbase.hosts=localhost",
                "couchbase.username=" + COUCHBASE_USERNAME,
                "couchbase.password=" + COUCHBASE_PASSWORD,
                "organizations.bucket=organizations",
                "token.expiration=" + API_KEY_TTL_IN_SECONDS * 1000,
                "io.scalecube.configuration.seeds=localhost:4801",
                "io.scalecube.configuration.discoveryPort=4803",
                "io.scalecube.configuration.servicePort=5803",
                "couchbase.bucketName=configurations",
                "api.keys.path.pattern=%s/api-keys/",
                "key.cache.ttl=2",
                "key.cache.refresh.interval=1")
            .waitingFor(new LogMessageWaitStrategy().withRegEx("^.*Vault server started!.*$"));
    vault.start();

    return vault;
  }

  private Microservices startGateway() {
    LOGGER.info("### Start gateway");

    return Microservices.builder()
        .discovery(
            serviceEndpoint ->
                new ScalecubeServiceDiscovery(serviceEndpoint).options(opts -> opts.port(4801)))
        .transport(
            opts ->
                opts.resources(RSocketTransportResources::new)
                    .client(RSocketServiceTransport.INSTANCE::clientTransport)
                    .server(RSocketServiceTransport.INSTANCE::serverTransport)
                    .port(5801))
        .gateway(options -> new WebsocketGateway(options.port(WS_GATEWAY_PORT)))
        .startAwait();
  }

  private Microservices startOrganizationService() {
    LOGGER.info("### Start organization service");

    return Microservices.builder()
        .discovery(
            serviceEndpoint ->
                new ScalecubeServiceDiscovery(serviceEndpoint)
                    .options(
                        opts -> opts.seedMembers(Address.create("localhost", 4801)).port(4802)))
        .transport(
            opts ->
                opts.resources(RSocketTransportResources::new)
                    .client(RSocketServiceTransport.INSTANCE::clientTransport)
                    .server(RSocketServiceTransport.INSTANCE::serverTransport)
                    .port(5802))
        .services(createOrganizationService())
        .startAwait();
  }

  private static OrganizationService createOrganizationService() {
    CouchbaseSettings settings =
        AppConfiguration.configRegistry()
            .objectProperty(couchbaseSettingsBindingMap(), CouchbaseSettings.class)
            .value()
            .orElseThrow(() -> new IllegalStateException("Couldn't load couchbase settings"));

    TokenVerifier tokenVerifier =
        token -> {
          Map<String, Object> claims = new HashMap<>();
          claims.put("aud", "scalecube");
          claims.put("role", "Owner");

          return Profile.builder().userId("scalecube_test_user").claims(claims).build();
        };

    return new OrganizationServiceImpl(
        new CouchbaseRepositoryFactory(settings).organizations(),
        new VaultKeyStore(),
        tokenVerifier);
  }

  private static Map<String, String> couchbaseSettingsBindingMap() {
    Map<String, String> bindingMap = new HashMap<>();

    bindingMap.put("hosts", "couchbase.hosts");
    bindingMap.put("username", "couchbase.username");
    bindingMap.put("password", "couchbase.password");
    bindingMap.put("organizationsBucketName", "organizations.bucket");

    return bindingMap;
  }

  private Microservices startConfigurationService() {
    LOGGER.info("### Start configuration service");

    ConfigRegistry configRegistry = AppConfiguration.configRegistry();

    DiscoveryOptions discoveryOptions =
        configRegistry
            .objectProperty("io.scalecube.configuration", DiscoveryOptions.class)
            .value()
            .orElseThrow(() -> new IllegalStateException("Couldn't load discovery options"));

    return Microservices.builder()
        .discovery(
            serviceEndpoint ->
                new ScalecubeServiceDiscovery(serviceEndpoint)
                    .options(
                        opts ->
                            opts.seedMembers(discoveryOptions.seeds())
                                .port(discoveryOptions.discoveryPort())
                                .memberHost(discoveryOptions.memberHost())
                                .memberPort(discoveryOptions.memberPort())))
        .transport(
            opts ->
                opts.resources(RSocketTransportResources::new)
                    .client(RSocketServiceTransport.INSTANCE::clientTransport)
                    .server(RSocketServiceTransport.INSTANCE::serverTransport)
                    .port(discoveryOptions.servicePort()))
        .services(createConfigurationService())
        .startAwait();
  }

  private ServiceProvider createConfigurationService() {
    ConfigRegistry configRegistry = io.scalecube.configuration.AppConfiguration.configRegistry();

    io.scalecube.configuration.repository.couchbase.CouchbaseSettings settings =
        configRegistry
            .objectProperty(
                "couchbase",
                io.scalecube.configuration.repository.couchbase.CouchbaseSettings.class)
            .value(null);

    ConfigurationRepository configurationRepository =
        new CouchbaseRepository(couchbaseBucket(settings));

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

  private AsyncBucket couchbaseBucket(
      io.scalecube.configuration.repository.couchbase.CouchbaseSettings settings) {
    return Mono.fromCallable(
            () ->
                CouchbaseCluster.create(settings.hosts())
                    .authenticate(settings.username(), settings.password())
                    .openBucket(settings.bucketName())
                    .async())
        .retryBackoff(3, Duration.ofSeconds(1))
        .block(Duration.ofSeconds(30));
  }

  private void setEnv(Map<String, String> newenv) throws Exception {
    try {
      Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
      Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
      theEnvironmentField.setAccessible(true);
      Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
      env.putAll(newenv);
      Field theCaseInsensitiveEnvironmentField =
          processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
      theCaseInsensitiveEnvironmentField.setAccessible(true);
      Map<String, String> cienv =
          (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
      cienv.putAll(newenv);
    } catch (NoSuchFieldException e) {
      Class[] classes = Collections.class.getDeclaredClasses();
      Map<String, String> env = System.getenv();
      for (Class cl : classes) {
        if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
          Field field = cl.getDeclaredField("m");
          field.setAccessible(true);
          Object obj = field.get(env);
          Map<String, String> map = (Map<String, String>) obj;
          map.clear();
          map.putAll(newenv);
        }
      }
    }
  }
}
