package io.scalecube.configuration.fixtures;

import static io.scalecube.configuration.scenario.BaseScenario.API_KEY_TTL_IN_SECONDS;
import static io.scalecube.configuration.scenario.BaseScenario.KEY_CACHE_REFRESH_INTERVAL;
import static io.scalecube.configuration.scenario.BaseScenario.KEY_CACHE_TTL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.scalecube.account.api.AddOrganizationApiKeyRequest;
import io.scalecube.account.api.ApiKey;
import io.scalecube.account.api.CreateOrganizationResponse;
import io.scalecube.account.api.DeleteOrganizationResponse;
import io.scalecube.account.api.GetOrganizationRequest;
import io.scalecube.account.api.GetOrganizationResponse;
import io.scalecube.account.api.GetPublicKeyResponse;
import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.account.api.OrganizationInfo.Builder;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Role;
import io.scalecube.configuration.ConfigurationServiceImpl;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.authorization.DefaultPermissions;
import io.scalecube.configuration.repository.ConfigurationRepository;
import io.scalecube.configuration.repository.InMemoryConfigurationRepository;
import io.scalecube.configuration.tokens.OrganizationServiceKeyProvider;
import io.scalecube.security.acl.DefaultAccessControl;
import io.scalecube.security.api.AccessControl;
import io.scalecube.security.api.Authenticator;
import io.scalecube.security.api.Profile;
import io.scalecube.security.jwt.DefaultJwtAuthenticator;
import io.scalecube.test.fixtures.Fixture;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.mockito.ArgumentMatchers;
import org.opentest4j.TestAbortedException;
import reactor.core.publisher.Mono;

public final class InMemoryEnvironmentFixture implements Fixture {

  private final KeyPair keyPair;

  private ConfigurationService configurationService;
  private OrganizationService organizationService;

  public InMemoryEnvironmentFixture() throws NoSuchAlgorithmException {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(2048);
    keyPair = keyPairGenerator.generateKeyPair();
  }

  @Override
  public void setUp() throws TestAbortedException {
    organizationService = mockOrganization();

    ConfigurationRepository repository = new InMemoryConfigurationRepository();

    System.setProperty("key.cache.ttl", String.valueOf(KEY_CACHE_TTL));
    System.setProperty("key.cache.refresh.interval", String.valueOf(KEY_CACHE_REFRESH_INTERVAL));

    OrganizationServiceKeyProvider keyProvider =
        new OrganizationServiceKeyProvider(organizationService);

    Authenticator authenticator =
        new DefaultJwtAuthenticator(
            tokenClaims -> keyProvider.get(tokenClaims.get("kid").toString()));

    AccessControl accessControl =
        DefaultAccessControl.builder()
            .authenticator(authenticator)
            .authorizer(DefaultPermissions.PERMISSIONS)
            .build();

    configurationService = new ConfigurationServiceImpl(repository, accessControl);
  }

  @Override
  public <T> T proxyFor(Class<? extends T> clazz) {
    if (clazz.isAssignableFrom(ConfigurationService.class)) {
      return clazz.cast(configurationService);
    }

    if (clazz.isAssignableFrom(OrganizationService.class)) {
      return clazz.cast(organizationService);
    }

    throw new IllegalArgumentException("Unexpected type: " + clazz);
  }

  @Override
  public void tearDown() {
    // do nothing
  }

  private OrganizationService mockOrganization() {
    OrganizationService organizationService = mock(OrganizationService.class);

    String organizationId1 = "ORG-TEST-1";
    String organizationName1 = "Test_Organization_1";
    Builder organization1InfoBuilder =
        OrganizationInfo.builder()
            .id(organizationId1)
            .name(organizationName1)
            .email("info@scalecube.io");
    OrganizationInfo organization1Info = organization1InfoBuilder.build();

    String organizationId2 = "ORG-TEST-2";
    String organizationName2 = "Test_Organization_2";
    Builder organization2InfoBuilder =
        OrganizationInfo.builder()
            .id(organizationId2)
            .name(organizationName2)
            .email("info@scalecube.io");

    // mock create organizations
    when(organizationService.createOrganization(any()))
        .thenReturn(
            Mono.just(new CreateOrganizationResponse(organization1InfoBuilder)),
            Mono.just(new CreateOrganizationResponse(organization2InfoBuilder)));

    // mock get organization
    GetOrganizationRequest getOrganizationRequest1 =
        ArgumentMatchers.argThat(
            argument -> argument != null && argument.organizationId().equals(organizationId1));
    when(organizationService.getOrganization(getOrganizationRequest1))
        .thenReturn(Mono.just(new GetOrganizationResponse(organization1InfoBuilder)));

    GetOrganizationRequest getOrganizationRequest2 =
        ArgumentMatchers.argThat(
            argument -> argument != null && argument.organizationId().equals(organizationId2));
    when(organizationService.getOrganization(getOrganizationRequest2))
        .thenReturn(Mono.just(new GetOrganizationResponse(organization2InfoBuilder)));

    // mock add api key

    when(organizationService.addOrganizationApiKey(any()))
        .thenAnswer(
            invocation -> {
              AddOrganizationApiKeyRequest request = invocation.getArgument(0);

              return Mono.just(
                  new GetOrganizationResponse(
                      organization1InfoBuilder.apiKeys(
                          new ApiKey[] {
                            mockApiKey(
                                request.organizationId(),
                                Role.valueOf(request.claims().get("role")))
                          })));
            });

    // mock get public key
    when(organizationService.getPublicKey(any()))
        .thenReturn(
            Mono.just(
                new GetPublicKeyResponse(
                    keyPair.getPublic().getAlgorithm(),
                    keyPair.getPublic().getFormat(),
                    keyPair.getPublic().getEncoded(),
                    "key_id")))
        .thenReturn(Mono.empty());

    // mock delete organization
    when(organizationService.deleteOrganization(any()))
        .thenReturn(Mono.just(new DeleteOrganizationResponse(organization1Info.id(), true)));

    // mock delete organization api key
    when(organizationService.deleteOrganizationApiKey(any()))
        .thenReturn(Mono.just(new GetOrganizationResponse(organization2InfoBuilder)));

    return organizationService;
  }

  @SuppressWarnings("unchecked")
  private ApiKey mockApiKey(String organizationId, Role role) {
    ApiKey apiKey = mock(ApiKey.class);

    Map claims = new HashMap<>();
    claims.put("aud", organizationId);
    claims.put("role", role.name());

    Profile profile = Profile.builder().claims(claims).build();

    when(apiKey.name()).thenReturn(role + "_api_key_name");
    when(apiKey.key()).thenReturn(token(profile, keyPair.getPrivate()));
    when(apiKey.claims()).thenReturn(claims);

    return apiKey;
  }

  private String token(Profile profile, Key signingKey) {
    JwtBuilder tokenBuilder =
        Jwts.builder().setHeaderParam("kid", "42").addClaims(profile.claims());

    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.SECOND, API_KEY_TTL_IN_SECONDS);
    tokenBuilder.setExpiration(calendar.getTime());

    return tokenBuilder.signWith(signingKey).compact();
  }
}
