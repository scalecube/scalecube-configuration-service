package io.scalecube.configuration.fixtures;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.couchbase.client.java.cluster.DefaultBucketSettings;
import com.couchbase.client.java.cluster.UserRole;
import com.couchbase.client.java.cluster.UserSettings;
import com.github.dockerjava.api.model.PortBinding;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.scalecube.account.api.ApiKey;
import io.scalecube.account.api.DeleteOrganizationResponse;
import io.scalecube.account.api.GetMembershipResponse;
import io.scalecube.account.api.GetOrganizationRequest;
import io.scalecube.account.api.GetOrganizationResponse;
import io.scalecube.account.api.GetPublicKeyResponse;
import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.account.api.OrganizationInfo.Builder;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Role;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.organization.server.OrganizationServiceRunner;
import io.scalecube.security.api.Profile;
import io.scalecube.services.gateway.clientsdk.Client;
import io.scalecube.services.gateway.clientsdk.ClientSettings;
import io.scalecube.test.fixtures.Fixture;
import java.io.IOException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import org.mockito.ArgumentMatchers;
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

  private static final Logger logger = LoggerFactory
      .getLogger(ContainersConfigurationServiceFixture.class);

  private static final String COUCHBASE_DOCKER_IMAGE = "couchbase:community-6.0.0";
  private static final String COUCHBASE_USERNAME = "admin";
  private static final String COUCHBASE_PASSWORD = "123456";
  private static final String COUCHBASE_NETWORK_ALIAS = "couchbase";

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

  //  private Environment environment;
  private Client client;

  private final KeyPair keyPair;

  private ConfigurationService configurationService;
  private OrganizationService organizationService;
  private Future<?> orgServiceFuture;

  public ContainersConfigurationServiceFixture() throws NoSuchAlgorithmException {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(2048);
    keyPair = keyPairGenerator.generateKeyPair();
  }

  @Override
  public void setUp() throws TestAbortedException {
//    setUpCouchbase();
//    setUpVault();
//    setUpGateway();
//
//    Map<String, String> env = new HashMap<>();
//    env.put("VAULT_ADDR", String.format(VAULT_ADDR_PATTERN, VAULT_NETWORK_ALIAS, VAULT_PORT));
//    env.put("VAULT_SECRETS_PATH", VAULT_SECRETS_PATH);
//    env.put("VAULT_TOKEN", VAULT_TOKEN);
//
//    setUpOrganizationService(env);
//    setUpConfigurationService(env);

    ClientSettings clientSettings = ClientSettings.builder()
        .loopResources(LoopResources.create("ws" + "-loop")).host("localhost").port(7070).build();

    client = Client.websocket(clientSettings);
    organizationService = client.forService(OrganizationService.class);
//    organizationService = mockOrganization();
    configurationService = client.forService(ConfigurationService.class);

//    organizationService = client.forService(OrganizationService.class);

//    ConfigurationRepository repository = new InMemoryConfigurationRepository();
//
//    System.setProperty("key.cache.ttl", "2");
//    System.setProperty("key.cache.refresh.interval", "1");
//
//    OrganizationServiceKeyProvider keyProvider =
//        new OrganizationServiceKeyProvider(organizationService);
//
//    Authenticator authenticator =
//        new DefaultJwtAuthenticator(/**/
//            tokenClaims -> keyProvider.get(tokenClaims.get("kid").toString()).block());
//
//    AccessControl accessControl =
//        DefaultAccessControl.builder()
//            .authenticator(authenticator)
//            .authorizer(DefaultPermissions.PERMISSIONS)
//            .build();
//
//    configurationService = new ConfigurationServiceImpl(repository, accessControl);

  }

  @Override
  public <T> T proxyFor(Class<? extends T> clazz) {
//    return client.forService(clasz);
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
  }

  private void setUpOrganizationService(Map<String, String> env) {
    env.put("JAVA_OPTS", "-Dio.scalecube.organization.seeds=" + GATEWAY_NETWORK_ALIAS + ":4801");

    new GenericContainer<>("scalecube/scalecube-organization:latest")
        .withNetwork(Network.SHARED)
        .withNetworkAliases("scalecube-organization")
        .withCreateContainerCmdModifier(cmd -> cmd.withName("scalecube-organization"))
        .withEnv(env)
        .start();
  }

  private void setUpConfigurationService(Map<String, String> env) {
    env.put("JAVA_OPTS", "-Dio.scalecube.configuration.seeds=" + GATEWAY_NETWORK_ALIAS + ":4801");

    new GenericContainer<>("scalecube/scalecube-configuration:latest")
        .withNetwork(Network.SHARED)
        .withNetworkAliases("scalecube-configuration")
        .withCreateContainerCmdModifier(cmd -> cmd.withName("scalecube-configuration"))
        .withEnv(env)
        .waitingFor(new LogMessageWaitStrategy().withRegEx("^.*scalecube.*Running.*$"))
        .start();
  }

  private void setUpGateway() {
    GenericContainer gateway =
        new GenericContainer<>("scalecube/scalecube-services-gateway-runner:2.5.3")
            .withExposedPorts(WS_GATEWAY_PORT, HTTP_GATEWAY_PORT, RS_GATEWAY_PORT)
            .withNetwork(Network.SHARED)
            .withNetworkAliases(GATEWAY_NETWORK_ALIAS)
            .withCreateContainerCmdModifier(
                cmd -> {
                  cmd.withName(GATEWAY_NETWORK_ALIAS);
                  cmd.withPortBindings(
                      PortBinding.parse(WS_GATEWAY_PORT + ":" + WS_GATEWAY_PORT),
                      PortBinding.parse(HTTP_GATEWAY_PORT + ":" + HTTP_GATEWAY_PORT),
                      PortBinding.parse(RS_GATEWAY_PORT + ":" + RS_GATEWAY_PORT));
                })
            .waitingFor(new HostPortWaitStrategy());
    gateway.start();
  }

  private void setUpVault() {
    VaultContainer<?> vault =
        new VaultContainer<>(VAULT_DOCKER_IMAGE)
            .withVaultPort(VAULT_PORT)
            .withVaultToken(VAULT_TOKEN)
            .withSecretInVault(VAULT_SECRETS_PATH,
                "couchbase.hosts=" + COUCHBASE_NETWORK_ALIAS,
                "couchbase.username=" + COUCHBASE_USERNAME,
                "couchbase.password=" + COUCHBASE_PASSWORD)
            .withNetwork(Network.SHARED)
            .withNetworkAliases(VAULT_NETWORK_ALIAS)
            .withCreateContainerCmdModifier(cmd -> cmd.withName(VAULT_NETWORK_ALIAS))
            .waitingFor(new LogMessageWaitStrategy().withRegEx("^.*Vault server started!.*$"));
    vault.start();
  }


  private void setUpCouchbase() {
    CouchbaseContainer couchbase =
        new CouchbaseContainer(COUCHBASE_DOCKER_IMAGE)
            .withClusterAdmin(COUCHBASE_USERNAME, COUCHBASE_PASSWORD)
            .withNetwork(Network.SHARED)
            .withNetworkAliases(COUCHBASE_NETWORK_ALIAS)
            .withCreateContainerCmdModifier(
                cmd -> {
                  cmd.withName(COUCHBASE_NETWORK_ALIAS);
                  cmd.withPortBindings(PortBinding.parse("8091:8091"));
                });
    couchbase.start();
    couchbase.initCluster();
    try {
      couchbase.callCouchbaseRestAPI("/settings/indexes", "storageMode=forestdb");
    } catch (IOException e) {
      logger.warn("Couchbase set up issues", e);
    }

    String password = "123456";
    createBucket(couchbase, "organizations", password);
    createBucket(couchbase, "configurations", password);
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

  private OrganizationService mockOrganization() {
    OrganizationService organizationService = mock(OrganizationService.class);

    String organizationId1 = "ORG-TEST-1";
    Builder organization1InfoBuilder =
        OrganizationInfo.builder()
            .id(organizationId1)
            .name("Test Organization 1")
            .email("info@scalecube.io")
            .apiKeys(
                new ApiKey[]{
                    mockApiKey(organizationId1, Role.Owner),
                    mockApiKey(organizationId1, Role.Owner, true),
                    mockApiKey(organizationId1, Role.Admin),
                    mockApiKey(organizationId1, Role.Member)
                });
    OrganizationInfo organization1Info = organization1InfoBuilder.build();

    String organizationId2 = "ORG-TEST-2";
    Builder organization2InfoBuilder =
        OrganizationInfo.builder()
            .id(organizationId2)
            .name("Test Organization 2")
            .email("info@scalecube.io")
            .apiKeys(
                new ApiKey[]{
                    mockApiKey(organizationId2, Role.Owner),
                    mockApiKey(organizationId2, Role.Admin),
                    mockApiKey(organizationId2, Role.Member)
                });
    OrganizationInfo organization2Info = organization2InfoBuilder.build();

    GetOrganizationRequest request1 =
        ArgumentMatchers.argThat(
            argument -> argument != null && argument.organizationId().equals(organizationId1));
    when(organizationService.getOrganization(request1))
        .thenReturn(Mono.just(new GetOrganizationResponse(organization1InfoBuilder)));

    GetOrganizationRequest request2 =
        ArgumentMatchers.argThat(
            argument -> argument != null && argument.organizationId().equals(organizationId2));
    when(organizationService.getOrganization(request2))
        .thenReturn(Mono.just(new GetOrganizationResponse(organization2InfoBuilder)));

    when(organizationService.getUserOrganizationsMembership(any()))
        .thenReturn(
            Mono.just(
                new GetMembershipResponse(
                    new OrganizationInfo[]{organization1Info, organization2Info})));

    when(organizationService.getPublicKey(any()))
        .thenReturn(
            Mono.just(
                new GetPublicKeyResponse(
                    keyPair.getPublic().getAlgorithm(),
                    keyPair.getPublic().getFormat(),
                    keyPair.getPublic().getEncoded(),
                    "key_id")))
        .thenReturn(Mono.empty());

    when(organizationService.deleteOrganization(any()))
        .thenReturn(Mono.just(new DeleteOrganizationResponse(organization1Info.id(), true)));

    when(organizationService.deleteOrganizationApiKey(any()))
        .thenReturn(Mono.just(new GetOrganizationResponse(organization2InfoBuilder)));

    return organizationService;
  }

  private ApiKey mockApiKey(String organizationId, Role role) {
    return mockApiKey(organizationId, role, false);
  }

  @SuppressWarnings("unchecked")
  private ApiKey mockApiKey(String organizationId, Role role, boolean expired) {
    ApiKey apiKey = mock(ApiKey.class);

    Map claims = new HashMap<>();
    claims.put("aud", organizationId);
    claims.put("role", role.name());

    Profile profile = Profile.builder().claims(claims).build();

    when(apiKey.name()).thenReturn(expired ? "expired" : role + "_api_key_name");
    when(apiKey.key()).thenReturn(token(profile, keyPair.getPrivate(), expired));
    when(apiKey.claims()).thenReturn(claims);

    return apiKey;
  }

  private String token(Profile profile, Key signingKey, boolean expired) {
    return token(
        profile,
        options -> {
          if (expired) {
            options.setExpiration(new Date());
          }
        },
        signingKey);
  }

  private String token(Profile profile, Consumer<JwtBuilder> options, Key signingKey) {
    JwtBuilder tokenBuilder =
        Jwts.builder().setHeaderParam("kid", "42").addClaims(profile.claims());

    options.accept(tokenBuilder);

    return tokenBuilder.signWith(signingKey).compact();
  }
}
