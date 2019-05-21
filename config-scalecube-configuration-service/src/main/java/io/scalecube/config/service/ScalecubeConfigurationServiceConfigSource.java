package io.scalecube.config.service;

import static io.scalecube.services.gateway.clientsdk.Client.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.scalecube.config.ConfigProperty;
import io.scalecube.config.ConfigSourceNotAvailableException;
import io.scalecube.config.source.ConfigSource;
import io.scalecube.config.source.LoadedConfigProperty;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.EntriesRequest;
import io.scalecube.configuration.api.FetchResponse;
import io.scalecube.services.gateway.clientsdk.ClientSettings;
import io.scalecube.services.transport.jackson.JacksonCodec;
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

    private static final int HTTP_PORT = 80;
    private static final int HTTPS_PORT = 443;

    private String token;
    private String repository;
    private URL url = null;
    private ConfigurationService service = null;

    private Builder() {}

    private Builder(ConfigurationService service) {
      this.service = service;
    }

    /**
     * Create a ScalecubeConfigurationServiceConfigSource from this builder.
     *
     * @return a new ScalecubeConfigurationServiceConfigSource.
     */
    public ScalecubeConfigurationServiceConfigSource build() {
      if (Objects.requireNonNull(this.token, "Missing token").isEmpty()) {
        throw new IllegalArgumentException("Missing token");
      }
      if (Objects.requireNonNull(this.repository, "Missing repository").isEmpty()) {
        throw new IllegalArgumentException("Missing repository");
      }
      if (this.service == null) {
        Objects.requireNonNull(this.url);
        ClientSettings.Builder builder =
            ClientSettings.builder()
                .host(url.getHost())
                .contentType(JacksonCodec.CONTENT_TYPE)
                .loopResources(HttpResources.get());

        if ("https".equals(url.getProtocol())) {
          if (url.getPort() != -1) {
            builder.port(url.getPort());
          } else {
            builder.port(HTTPS_PORT);
          }
          builder = builder.secure();
        } else if ("http".equals(url.getProtocol())) {
          if (url.getPort() != -1) {
            builder.port(url.getPort());
          } else {
            builder.port(HTTP_PORT);
          }
        } else {
          throw new IllegalArgumentException("Unkowon protocol");
        }
        this.service = http(builder.build()).forService(ConfigurationService.class);
      }
      Objects.requireNonNull(this.service, "No URL and no Service was set");

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

    public Builder url(URL url) {
      this.url = url;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  static Builder builder(ConfigurationService service) {
    return new Builder(service);
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
      LOGGER.warn("unable to load config properties", e);
      throw new ConfigSourceNotAvailableException(e);
    }
  }

  private static class Parsing {
    private ObjectWriter writer;

    private Parsing() {
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
