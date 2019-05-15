package io.scalecube.config.service;

import static io.scalecube.services.gateway.clientsdk.ClientSettings.builder;

import com.fasterxml.jackson.core.type.TypeReference;
import io.scalecube.config.ConfigProperty;
import io.scalecube.config.ConfigSourceNotAvailableException;
import io.scalecube.config.source.ConfigSource;
import io.scalecube.config.source.LoadedConfigProperty;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.EntriesRequest;
import io.scalecube.services.gateway.clientsdk.Client;
import io.scalecube.services.gateway.clientsdk.ClientCodec;
import io.scalecube.services.gateway.clientsdk.ClientMessage;
import io.scalecube.services.gateway.clientsdk.ClientSettings;
import io.scalecube.services.gateway.clientsdk.ClientSettings.Builder;
import io.scalecube.services.gateway.clientsdk.http.HttpClientCodec;
import io.scalecube.services.transport.api.DataCodec;
import io.scalecube.services.transport.jackson.JacksonCodec;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.http.HttpResources;

public class ScalecubeConfigurationServiceConfigSource implements ConfigSource {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(ScalecubeConfigurationServiceConfigSource.class);

  private ClientCodec clientCodec;
  Client client;
  private ClientMessage requestEntries;

  private static URL defaultUrl() {
    try {
      return new URL("https", "configuration-service-http.genesis.om2.com", 443, "/");
    } catch (MalformedURLException ignoredException) {
      return null;
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

    ClientSettings clientSettings = builder.build();
    DataCodec dataCodec = DataCodec.getInstance(clientSettings.contentType());
    clientCodec = new HttpClientCodec(dataCodec);
    client = Client.http(clientSettings);

    requestEntries =
        ClientMessage.builder()
            .qualifier(ConfigurationService.CONFIG_ENTRIES)
            .data(new EntriesRequest(token, repository))
            .build();
  }

  @Override
  public Map<String, ConfigProperty> loadConfig() {

    ClientMessage clientMessage =
        client
            .requestResponse(requestEntries)
            .onErrorMap(
                e -> {
                  e.printStackTrace();
                  LOGGER.warn("unable to load config properties", e);
                  throw new ConfigSourceNotAvailableException(e);
                })
            .block();

    TypeReference<List<Entry>> typeReference = new TypeReference<List<Entry>>() {};

    List<Entry> entries = clientCodec.decodeData(clientMessage, typeReference.getType()).data();

    return entries.stream()
        .collect(
            Collectors.toMap(
                entry -> entry.key,
                entry -> LoadedConfigProperty.withNameAndValue(entry.key, entry.value).build()));
  }

  private static class Entry {
    private String value;
    private String key;
  }
}
