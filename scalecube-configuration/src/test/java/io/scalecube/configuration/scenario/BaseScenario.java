package io.scalecube.configuration.scenario;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.scalecube.account.api.ApiKey;
import io.scalecube.account.api.GetMembershipRequest;
import io.scalecube.account.api.GetMembershipResponse;
import io.scalecube.account.api.GetOrganizationRequest;
import io.scalecube.account.api.GetOrganizationResponse;
import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.configuration.ITInitBase;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import reactor.test.StepVerifier;

public abstract class BaseScenario {

  public static final Duration TIMEOUT = Duration.ofSeconds(15);
  static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static final Token AUTH0_TOKEN = new Token("AUTH0_TOKEN_MOCK");
  public static final String ORGANIZATION_1 = "Test_Organization_Name_1";
  public static final String ORGANIZATION_2 = "Test_Organization_Name_2";

  private ITInitBase itInitBase;

  public BaseScenario(ITInitBase itInitBase) {
    this.itInitBase = itInitBase;
  }

  public BaseScenario() {
  }

  @BeforeAll
  static void beforeAll() {
    StepVerifier.setDefaultTimeout(TIMEOUT);
  }

  @BeforeEach
  void before(OrganizationService organizationService) throws InterruptedException {
    if (itInitBase != null) {
      itInitBase.before(organizationService);
    }
  }

  @AfterEach
  void after(OrganizationService organizationService) throws InterruptedException {
    if (itInitBase != null) {
      itInitBase.after(organizationService);
    }
  }

  protected OrganizationInfo getOrganization(
      OrganizationService organizationService, String organizationName) {
    GetMembershipResponse membership =
        organizationService
            .getUserOrganizationsMembership(new GetMembershipRequest(AUTH0_TOKEN))
            .block(TIMEOUT);

    return Stream.of(
        Optional.ofNullable(membership)
            .orElseThrow(() -> new IllegalStateException("Membership is null"))
            .organizations())
        .filter(organizationInfo -> organizationName.equals(organizationInfo.name()))
        .findAny()
        .orElseThrow(
            () -> new IllegalStateException("Organization '" + organizationName + "' not found"));
  }

  ApiKey getExpiredApiKey(
      OrganizationService organizationService, String organizationId, Role role) {
    return getApiKey(organizationService, organizationId, role, true);
  }

  protected ApiKey getApiKey(OrganizationService organizationService, String organizationId,
      Role role) {
    return getApiKey(organizationService, organizationId, role, false);
  }

  private ApiKey getApiKey(
      OrganizationService organizationService, String organizationId, Role role, boolean expired) {
    GetOrganizationResponse organization =
        organizationService
            .getOrganization(new GetOrganizationRequest(AUTH0_TOKEN, organizationId))
            .block(TIMEOUT);

    return Stream.of(
        Optional.ofNullable(organization)
            .orElseThrow(() -> new IllegalStateException("Organization is null"))
            .apiKeys())
        .filter(
            apiKey -> {
              if (expired) {
                return apiKey.name().equals("expired");
              }
              return true;
            })
        .filter(apiKey -> role.name().equals(apiKey.claims().get("role")))
        .findAny()
        .orElseThrow(() -> new IllegalStateException("ApiKey is null"));
  }

  protected static Map valueMap(Object value) {
    if (value instanceof Map) {
      return (Map) value;
    }

    if (value instanceof ObjectNode) {
      Iterable iterable = () -> ((ObjectNode) value).fields();
      return (Map) StreamSupport.stream(iterable.spliterator(), false)
          .collect(Collectors.toMap(e -> ((Map.Entry) e).getKey(),
              e -> {
                Object val = ((Map.Entry) e).getValue();
                return val instanceof TextNode ? ((TextNode) val).textValue()
                    : ((IntNode) val).intValue();
              }));
    }

    throw new UnsupportedOperationException(value + "type is unsupported");
  }
}
