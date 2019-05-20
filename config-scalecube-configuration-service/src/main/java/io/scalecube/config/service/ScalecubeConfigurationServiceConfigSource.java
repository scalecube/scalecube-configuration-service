package io.scalecube.config.service;

import static io.scalecube.services.gateway.clientsdk.Client.http;
import static io.scalecube.services.gateway.clientsdk.ClientSettings.builder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.scalecube.config.ConfigProperty;
import io.scalecube.config.ConfigSourceNotAvailableException;
import io.scalecube.config.source.ConfigSource;
import io.scalecube.config.source.LoadedConfigProperty;
import io.scalecube.config.utils.ThrowableUtil;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.EntriesRequest;
import io.scalecube.configuration.api.FetchResponse;
import io.scalecube.services.gateway.clientsdk.ClientSettings.Builder;
import io.scalecube.services.transport.jackson.JacksonCodec;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.http.HttpResources;

public class ScalecubeConfigurationServiceConfigSource implements ConfigSource {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(ScalecubeConfigurationServiceConfigSource.class);

  private final ConfigurationService service;

  private final EntriesRequest requestEntries;

  private final Parsing parsing = new Parsing();

  private static URL defaultUrl() {
    try {
      return new URL("https", "configuration-service-http.genesis.om2.com", 443, "/");
    } catch (MalformedURLException urlException) {
      throw ThrowableUtil.propagate(urlException);
    }
  }

  /**
   * Create a configuration source that connects to the production environment of scalecube
   * configuration service.
   *
   * @param token the API token
   * @param repository the name of the repository
   */
  public ScalecubeConfigurationServiceConfigSource(String token, String repository) {
    this(token, repository, defaultUrl());
  }

  /**
   * Create a configuration source that connects to the production environment of scalecube
   * configuration service.
   *
   * @param token the API token
   * @param repository the name of the repository
   * @param service the URL of this service
   */
  public ScalecubeConfigurationServiceConfigSource(String token, String repository, URL service) {
    Builder builder =
        builder()
            .host(service.getHost())
            .port(service.getPort())
            .contentType(JacksonCodec.CONTENT_TYPE)
            .loopResources(HttpResources.get());
    if ("https".equals(service.getProtocol()) || "wss".equals(service.getProtocol())) {
      builder = builder.secure();
    }
    this.service = http(builder.build()).forService(ConfigurationService.class);
    requestEntries = new EntriesRequest(token, repository);
  }

  /**
   * Create a configuration source that connects to the production environment of scalecube
   * configuration service.
   *
   * @param token the API token
   * @param repository the name of the repository
   * @param service the actual configuration service
   */
  public ScalecubeConfigurationServiceConfigSource(
      String token, String repository, ConfigurationService service) {
    this.service = service;
    requestEntries = new EntriesRequest(token, repository);
  }

  ConfigurationService service() {
    return service;
  }
  @Override
  public Map<String, ConfigProperty> loadConfig() {
    try {
      return service
          .entries(requestEntries)
          .flatMapIterable(Function.identity())
          .collectMap(FetchResponse::key, this.parsing::fromFetchResponse)
          .block();
    } catch (Exception e) {
      e.printStackTrace();
      LOGGER.warn("unable to load config properties", e);
      throw new ConfigSourceNotAvailableException(e);
    }
  }

  private static class Parsing {
    private ObjectWriter writer;

    protected Parsing() {
      writer = ObjectMapperHolder.getInstance().writer(new MinimalPrettyPrinter());
    }

    public ConfigProperty fromFetchResponse(FetchResponse fetchResponse) {
      try {
        return LoadedConfigProperty.withNameAndValue(
                fetchResponse.key(), writer.writeValueAsString(fetchResponse.value()))
            .build();
      } catch (JsonProcessingException ignoredException) {
        return LoadedConfigProperty.withNameAndValue(fetchResponse.key(), null).build();
      }
    }
  }
}
