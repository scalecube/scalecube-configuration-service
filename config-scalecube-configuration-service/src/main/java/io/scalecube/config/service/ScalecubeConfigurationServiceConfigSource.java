package io.scalecube.config.service;

import static io.scalecube.services.gateway.clientsdk.Client.http;

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
import io.scalecube.services.gateway.clientsdk.ClientSettings;
import io.scalecube.services.gateway.clientsdk.ClientSettings.Builder;
import io.scalecube.services.transport.jackson.JacksonCodec;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
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

  public static class Builder {
    private String token;
    private String repository;
    private URL url = ScalecubeConfigurationServiceConfigSource.defaultUrl();
    private ConfigurationService service;

    public Builder() {}

    public ScalecubeConfigurationServiceConfigSource build() {
      if (Objects.requireNonNull(this.token, "Missing token").isEmpty()) {
        throw new IllegalArgumentException("Missing token");
      }
      if (Objects.requireNonNull(this.repository, "Missing repository").isEmpty()) {
        throw new IllegalArgumentException("Missing repository");
      }
      if (this.service == null) {
        ClientSettings.Builder builder =
            ClientSettings.builder()
                .host(url.getHost())
                .port(url.getPort())
                .contentType(JacksonCodec.CONTENT_TYPE)
                .loopResources(HttpResources.get());
        if ("https".equals(url.getProtocol()) || "wss".equals(url.getProtocol())) {
          builder = builder.secure();
        }
        this.service = http(builder.build()).forService(ConfigurationService.class);
      }
      return new ScalecubeConfigurationServiceConfigSource(this);
    }

    public Builder token(String token) {
      this.token = token;
      return this;
    }

    public Builder repository(String repository) {
      this.repository = repository;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private static URL defaultUrl() {
    try {
      return new URL("https", "configuration-service-http.genesis.om2.com", 443, "/");
    } catch (MalformedURLException urlException) {
      throw ThrowableUtil.propagate(urlException);
    }
  }

  public ScalecubeConfigurationServiceConfigSource(Builder builder) {
    this.service = builder.service;
    this.requestEntries = new EntriesRequest(builder.token, builder.repository);
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
