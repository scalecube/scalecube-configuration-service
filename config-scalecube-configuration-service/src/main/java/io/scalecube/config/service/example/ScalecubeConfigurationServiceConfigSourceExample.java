package io.scalecube.config.service.example;

import static io.scalecube.services.gateway.clientsdk.Client.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.scalecube.config.ConfigRegistry;
import io.scalecube.config.ConfigRegistrySettings;
import io.scalecube.config.ObjectConfigProperty;
import io.scalecube.config.audit.Slf4JConfigEventListener;
import io.scalecube.config.service.ObjectMapperHolder;
import io.scalecube.config.service.ScalecubeConfigurationServiceConfigSource;
import io.scalecube.config.utils.ThrowableUtil;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.CreateEntryRequest;
import io.scalecube.services.gateway.clientsdk.ClientSettings;
import io.scalecube.services.gateway.clientsdk.ClientSettings.Builder;
import io.scalecube.services.transport.jackson.JacksonCodec;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import reactor.core.publisher.Flux;
import reactor.netty.http.HttpResources;

public class ScalecubeConfigurationServiceConfigSourceExample {

  private static final int RELOAD_INTERVAL_SEC = 1;

  private static final ObjectMapper objectMapper = ObjectMapperHolder.getInstance();

  /**
   * Main method of example of how to read json value from config registry.
   *
   * @param args program arguments
   * @throws IOException when the URL is invalid
   * @throws InterruptedException when {@link Thread#sleep(long)} was interrupted
   */
  public static void main(String[] args) throws IOException, InterruptedException {

    String repository = args[0];
    String token = args[1];
    URL url = new URL("https", "configuration-service-http.genesis.om2.com", 443, "/");
    Builder builder =
        ClientSettings.builder()
            .host(url.getHost())
            .port(url.getPort())
            .contentType(JacksonCodec.CONTENT_TYPE)
            .loopResources(HttpResources.get())
            .secure();

    ConfigurationService configurationService =
        http(builder.build()).forService(ConfigurationService.class);

    String key1 = "person1";
    JsonNode value1 = objectMapper.reader().readTree("{\"name\":\"foo\",\"age\":42}");
    CreateEntryRequest request = new CreateEntryRequest(token, repository, key1, value1);
    configurationService.createEntry(request).block();

    ConfigRegistrySettings configRegistrySettings =
        ConfigRegistrySettings.builder()
            .reloadIntervalSec(RELOAD_INTERVAL_SEC)
            .jmxEnabled(false)
            .addListener(new Slf4JConfigEventListener())
            .addLastSource(
                "ConfigurationService",
                ScalecubeConfigurationServiceConfigSource.builder()
                    .repository(repository)
                    .token(token)
                    .url(url)
                    .build())
            .build();
    ConfigRegistry configRegistry = ConfigRegistry.create(configRegistrySettings);

    ObjectConfigProperty<Person> entity1 =
        configRegistry.objectProperty(key1, mapper(Person.class));

    TimeUnit.SECONDS.sleep(RELOAD_INTERVAL_SEC);
    System.out.println("entity = " + entity1.value());

    Flux.interval(Duration.ofSeconds(1))
        .flatMapIterable(nothing -> configRegistry.allProperties())
        .filter(keyName -> keyName.startsWith("person")) // convention!
        .distinct() // accept only new values.
        .subscribe(
            key -> {
              ObjectConfigProperty<Person> entity =
                  configRegistry.objectProperty(key, mapper(Person.class));
              entity.addCallback(
                  (oldPersonConfig, newPersonConfig) -> {
                    System.out.println(
                        " << "
                            + Thread.currentThread().getName()
                            + " >> New person config: was ["
                            + oldPersonConfig.toString()
                            + "] and now its ["
                            + newPersonConfig
                            + "]");
                  });
            });
    Flux.interval(Duration.ofSeconds(1))
        .take(20)
        .map(l -> 42 + l)
        .map(
            newAgeOfFoo -> {
              try {
                System.out.println(
                    " << "
                        + Thread.currentThread().getName()
                        + " >> Saving new value: "
                        + newAgeOfFoo);
                return new CreateEntryRequest(
                    token,
                    repository,
                    "person3",
                    ObjectMapperHolder.getInstance()
                        .reader()
                        .readTree("{\"name\":\"person2\",\"age\":" + newAgeOfFoo + "}"));
              } catch (IOException ignoredException) {
                throw ThrowableUtil.propagate(ignoredException);
              }
            })
        .flatMap(configurationService::createEntry)
        .blockLast();
  }

  private static <T> Function<String, T> mapper(Class<T> clazz) {
    return value -> {
      try {
        return objectMapper.readValue(value, clazz);
      } catch (Exception e) {
        throw ThrowableUtil.propagate(e);
      }
    };
  }
}
