package io.scalecube.configuration.fixtures;

import static io.scalecube.configuration.fixtures.EnvUtils.setEnv;
import static io.scalecube.configuration.scenario.BaseScenario.API_KEY_TTL_IN_SECONDS;
import static io.scalecube.configuration.scenario.BaseScenario.KEY_CACHE_REFRESH_INTERVAL;
import static io.scalecube.configuration.scenario.BaseScenario.KEY_CACHE_TTL;

import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.bucket.BucketManager;
import com.couchbase.client.java.cluster.DefaultBucketSettings;
import com.couchbase.client.java.cluster.UserRole;
import com.couchbase.client.java.cluster.UserSettings;
import com.couchbase.client.java.document.JsonArrayDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.view.DefaultView;
import com.couchbase.client.java.view.DesignDocument;
import com.couchbase.client.java.view.DesignDocument.Option;
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
import io.scalecube.net.Address;
import io.scalecube.organization.OrganizationServiceImpl;
import io.scalecube.organization.config.AppConfiguration;
import io.scalecube.organization.repository.OrganizationsRepository;
import io.scalecube.organization.repository.couchbase.CouchbaseOrganizationsRepository;
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
import io.scalecube.services.gateway.ws.WebsocketGateway;
import io.scalecube.services.transport.rsocket.RSocketServiceTransport;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.couchbase.CouchbaseContainer;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;
import org.testcontainers.vault.VaultContainer;
import reactor.core.publisher.Mono;

final class IntegrationEnvironment {

  private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationEnvironmentFixture.class);

  private static final String COUCHBASE_DOCKER_IMAGE = "couchbase:community-6.0.0";
  private static final String COUCHBASE_USERNAME = "admin";
  private static final String COUCHBASE_PASSWORD = "123456";
  private static final String BUCKET_FULL_ACCESS = "bucket_full_access";

  private static final String VAULT_DOCKER_IMAGE = "vault:0.9.5";
  private static final int VAULT_PORT = 8200;
  private static final String VAULT_TOKEN = "token_for_benchmarks";
  private static final String VAULT_SECRETS_PATH = "secret/configuration-service/dev";
  private static final String VAULT_ADDR_PATTERN = "http://%s:%d";

  private static final int GATEWAY_WS_PORT = 7070;
  private static final int GATEWAY_DISCOVERY_PORT = 4801;
  private static final int GATEWAY_TRANSPORT_PORT = 5801;

  private static final int ORG_SERVICE_DISCOVERY_PORT = 4802;
  private static final int ORG_SERVICE_TRANSPORT_PORT = 5802;

  private static final int CONF_SERVICE_DISCOVERY_PORT = 4803;
  private static final int CONF_SERVICE_TRANSPORT_PORT = 5803;

  private CouchbaseContainer couchbase;
  private VaultContainer vault;
  private Microservices gateway;
  private Microservices organizationService;
  private Microservices configurationService;

  void start() {
    LOGGER.info("### Start environment");

    try {
      couchbase = startCouchbase();
      vault = startVault();
      gateway = startGateway();

      setEnv("VAULT_ADDR", String.format(VAULT_ADDR_PATTERN, "localhost", VAULT_PORT));
      setEnv("VAULT_SECRETS_PATH", VAULT_SECRETS_PATH);
      setEnv("VAULT_TOKEN", VAULT_TOKEN);

      organizationService = startOrganizationService();
      configurationService = startConfigurationService();

    } catch (Exception e) {
      LOGGER.error("### Error on environment set up", e);

      stop();

      throw new RuntimeException("Error on environment set up", e);
    }

    LOGGER.info("### Environment is running");
  }

  void stop() {
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

      TimeUnit.SECONDS.sleep(5);
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
                cmd -> {
                  cmd.withName("couchbase-" + RandomStringUtils.randomAlphabetic(5));
                  cmd.withPortBindings(PortBinding.parse("8091:8091"));
                });
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

    couchbaseInit(couchbase, configName);

    couchbase.getCouchbaseCluster().disconnect();
    couchbase.getCouchbaseEnvironment().shutdown();

    return couchbase;
  }

  private static void couchbaseInit(CouchbaseContainer couchbase, String bucketName) {
    Bucket bucket = couchbase.getCouchbaseCluster().openBucket(bucketName, COUCHBASE_PASSWORD);

    bucket.insert(JsonArrayDocument.create("repos", JsonArray.create()));

    BucketManager bucketManager = bucket.bucketManager();

    Map<Option, Long> options = new HashMap<>();
    options.put(Option.UPDATE_MIN_CHANGES, 1L);
    options.put(Option.REPLICA_UPDATE_MIN_CHANGES, 1L);

    DesignDocument designDoc =
        DesignDocument.create(
            "keys",
            Arrays.asList(
                DefaultView.create(
                    "by_keys",
                    "function (doc, meta) { "
                        + "  if (meta.id != 'repos') { "
                        + "    emit(meta.id.substring(0, meta.id.lastIndexOf('::')), null);"
                        + "  }"
                        + "}")),
            options);

    bucketManager.insertDesignDocument(designDoc);
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
                "io.scalecube.configuration.seeds=localhost:" + GATEWAY_DISCOVERY_PORT,
                "io.scalecube.configuration.discoveryPort=" + CONF_SERVICE_DISCOVERY_PORT,
                "io.scalecube.configuration.servicePort=" + CONF_SERVICE_TRANSPORT_PORT,
                "couchbase.bucketName=configurations",
                "api.keys.path.pattern=%s/api-keys/",
                "key.cache.ttl=" + KEY_CACHE_TTL,
                "key.cache.refresh.interval=" + KEY_CACHE_REFRESH_INTERVAL)
            .withCreateContainerCmdModifier(
                cmd -> cmd.withName("vault-" + RandomStringUtils.randomAlphabetic(5)))
            .waitingFor(new LogMessageWaitStrategy().withRegEx("^.*Vault server started!.*$"));
    vault.start();

    return vault;
  }

  private Microservices startGateway() {
    LOGGER.info("### Start gateway");

    return Microservices.builder()
        .discovery(
            serviceEndpoint ->
                new ScalecubeServiceDiscovery(serviceEndpoint)
                    .options(opts -> opts.port(GATEWAY_DISCOVERY_PORT)))
        .transport(
            opts ->
                opts.serviceTransport(RSocketServiceTransport::new).port(GATEWAY_TRANSPORT_PORT))
        .gateway(options -> new WebsocketGateway(options.port(GATEWAY_WS_PORT)))
        .startAwait();
  }

  private Microservices startOrganizationService() {
    LOGGER.info("### Start organization service");

    return Microservices.builder()
        .discovery(
            serviceEndpoint ->
                new ScalecubeServiceDiscovery(serviceEndpoint)
                    .options(
                        opts ->
                            opts.seedMembers(Address.create("localhost", GATEWAY_DISCOVERY_PORT))
                                .port(ORG_SERVICE_DISCOVERY_PORT)))
        .transport(
            opts ->
                opts.serviceTransport(RSocketServiceTransport::new)
                    .port(ORG_SERVICE_TRANSPORT_PORT))
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

          return Mono.just(Profile.builder().userId("scalecube_test_user").claims(claims).build());
        };

    Cluster cluster = CouchbaseCluster.create(settings.hosts());

    AsyncBucket bucket =
        Mono.fromCallable(
                () ->
                    cluster
                        .authenticate(settings.username(), settings.password())
                        .openBucket(settings.organizationsBucketName())
                        .async())
            .retryBackoff(3, Duration.ofSeconds(1))
            .block(Duration.ofSeconds(30));

    OrganizationsRepository repository = new CouchbaseOrganizationsRepository(bucket);

    return new OrganizationServiceImpl(repository, new VaultKeyStore(), tokenVerifier);
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
                opts.serviceTransport(RSocketServiceTransport::new)
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
          new DefaultJwtAuthenticator(map -> keyProvider.get(map.get("kid").toString()));

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
}
