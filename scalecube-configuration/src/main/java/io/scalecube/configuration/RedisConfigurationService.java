package io.scalecube.configuration;

//import io.scalecube.account.api.AccountService;
import io.scalecube.configuration.api.AccessRequest;
import io.scalecube.configuration.api.Acknowledgment;
import io.scalecube.configuration.api.ConfigurationService;
import io.scalecube.configuration.api.CreateRepositoryRequest;
import io.scalecube.configuration.api.DeleteRequest;
import io.scalecube.configuration.api.Entries;
import io.scalecube.configuration.api.FetchRequest;
import io.scalecube.configuration.api.FetchResponse;
import io.scalecube.configuration.api.SaveRequest;
import io.scalecube.configuration.repository.Document;
import io.scalecube.configuration.repository.redis.RedisStore;
import io.scalecube.services.Microservices;
import io.scalecube.services.annotations.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Mono;

import java.util.Map;

import javax.annotation.PostConstruct;

public class RedisConfigurationService implements ConfigurationService {

  private static final String PERMISSIONS_LEVEL = "permissions-level";
  private static final Logger LOGGER = LoggerFactory.getLogger(RedisConfigurationService.class);

  @Inject
  private Microservices microservices;
  //private AccountService accountService;

  private final RedisStore<Document> store;

  @PostConstruct
  void init() {

    //accountService = microservices.call().create().api(AccountService.class);
  }

  public static class Builder {

    //private AccountService accountService;

    public Builder redisson() {
      return this;
    }

    public Builder mock(/*AccountService accountService*/ Object obj) {
//      this.accountService = accountService;
      return this;
    }

    /**
     * build a Service instance.
     *
     * @return new initialzed service instance.
     */
    public RedisConfigurationService build() {
      return null;
          //new RedisConfigurationService(new RedisStore<>(redisson), /*accountService*/
          //null);
    }

  }

  public static Builder builder() {
    return new Builder();
  }

  private RedisConfigurationService(RedisStore<Document> store,
      Object accountService) {
      //AccountService accountService) {
    this.store = store;
//    this.accountService = accountService;
  }

  private enum Permissions {
    read, write
  }

  @Override
  public Mono<Acknowledgment> createRepository(CreateRepositoryRequest request) {
    return null;
  }

  @Override
  public Mono<FetchResponse> fetch(final FetchRequest request) {
    LOGGER.debug("received fetch request {}", request);
    return null;
//    accountService.register(request.token()).map(user -> {
//      final Document result = store.get(getCollectionName(request), request.key());
//      return result != null ? new FetchResponse(result.key(), result.value()) : FetchResponse.builder().build();    });
  }

  private String getCollectionName(final AccessRequest request) {
    return null;
//    return request.repository() + "@" + request.token().origin();
  }

  @Override
  public Mono<Entries<FetchResponse>> entries(final FetchRequest request) {
    return null;
//        accountService.register(request.token()).map(user -> {
//      FetchResponse[] fetchResponses = store.entries(getCollectionName(request)).stream()
//          .map(doc -> FetchResponse.builder()
//              .key(doc.key())
//              .value(doc.value())
//              .build())
//          .toArray(FetchResponse[]::new);
//      return new Entries<>(fetchResponses);
//    });
  }

  @Override
  public Mono<Acknowledgment> save(SaveRequest request) {
    return null;
//    return accountService.register(request.token())
//        .flatMap(user -> {
//          if (getPermissions(user.claims()).ordinal() >= Permissions.write.ordinal()) {
//            Document doc = Document.builder()
//                .key(request.key())
//                .value(request.value())
//                .build();
//            store.put(getCollectionName(request), doc.key(), doc);
//            return Mono.just(new Acknowledgment());
//          } else {
//            return Mono
//                .error(new InvalidPermissionsException("invalid permissions-level save request requires write access"));
//          }
//        });
  }


  @Override
  public Mono<Acknowledgment> delete(DeleteRequest request) {
    return null;
//    accountService.register(request.token())
//        .flatMap(user -> {
//          if (getPermissions(user.claims()).ordinal() >= Permissions.write.ordinal()) {
//            store.remove(getCollectionName(request), request.key());
//            return Mono.just(new Acknowledgment());
//          } else {
//            return Mono.error(
//                new InvalidPermissionsException("invalid permissions-level delete request requires write access"));
//          }
//        });

  }

  private Permissions getPermissions(Map<String, String> claims) {
    if (claims != null && claims.containsKey(PERMISSIONS_LEVEL)) {
      return Permissions.valueOf(claims.get(PERMISSIONS_LEVEL));
    }
    return Permissions.read;
  }
}
