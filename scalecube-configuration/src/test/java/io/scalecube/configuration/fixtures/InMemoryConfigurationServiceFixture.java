package io.scalecube.configuration.fixtures;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.mockito.ArgumentMatchers;
import org.opentest4j.TestAbortedException;
import reactor.core.publisher.Mono;

public final class InMemoryConfigurationServiceFixture implements Fixture {

  private final KeyPair keyPair;

  private ConfigurationService configurationService;
  private OrganizationService organizationService;

  public InMemoryConfigurationServiceFixture() throws NoSuchAlgorithmException {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(2048);
    keyPair = keyPairGenerator.generateKeyPair();
  }

  @Override
  public void setUp() throws TestAbortedException {
    organizationService = mockOrganization();

    ConfigurationRepository repository = new InMemoryConfigurationRepository();

    System.setProperty("key.cache.ttl", "2");
    System.setProperty("key.cache.refresh.interval", "1");

    OrganizationServiceKeyProvider keyProvider =
        new OrganizationServiceKeyProvider(organizationService);

    Authenticator authenticator =
        new DefaultJwtAuthenticator(
            tokenClaims -> keyProvider.get(tokenClaims.get("kid").toString()).block());

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
    Builder organization1InfoBuilder =
        OrganizationInfo.builder()
            .id(organizationId1)
            .name("Test Organization 1")
            .email("info@scalecube.io")
            .apiKeys(
                new ApiKey[] {
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
                new ApiKey[] {
                  mockApiKey(organizationId2, Role.Owner), mockApiKey(organizationId2, Role.Admin)
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
                    new OrganizationInfo[] {organization1Info, organization2Info})));

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
