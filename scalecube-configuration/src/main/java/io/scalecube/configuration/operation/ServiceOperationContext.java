package io.scalecube.configuration.operation;

import io.scalecube.configuration.authorization.AuthorizationService;
import io.scalecube.configuration.authorization.OperationType;
import io.scalecube.configuration.repository.ConfigurationDataAccess;
import io.scalecube.configuration.tokens.TokenVerifier;

public final class ServiceOperationContext {

  private final ConfigurationDataAccess dataAccess;
  private final TokenVerifier tokenVerifier;
  private final AuthorizationService authorizationService;
  private final OperationType operationType;

  private ServiceOperationContext(Builder builder) {
    this.dataAccess = builder.dataAccess;
    this.tokenVerifier = builder.tokenVerifier;
    this.authorizationService = builder.authorizationService;
    this.operationType = builder.operationType;
  }

  ConfigurationDataAccess dataAccess() {
    return dataAccess;
  }

  TokenVerifier tokenVerifier() {
    return tokenVerifier;
  }

  AuthorizationService authorizationService() {
    return authorizationService;
  }

  OperationType operationType() {
    return operationType;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private ConfigurationDataAccess dataAccess;
    private TokenVerifier tokenVerifier;
    private AuthorizationService authorizationService;
    private OperationType operationType;

    public Builder dataAccess(ConfigurationDataAccess dataAccess) {
      this.dataAccess = dataAccess;
      return this;
    }

    public Builder tokenVerifier(TokenVerifier tokenVerifier) {
      this.tokenVerifier = tokenVerifier;
      return this;
    }

    public Builder operationType(AuthorizationService authorizationService) {
      this.authorizationService = authorizationService;
      return this;
    }

    public Builder operationType(OperationType operationType) {
      this.operationType = operationType;
      return this;
    }

    public ServiceOperationContext build() {
      return new ServiceOperationContext(this);
    }
  }
}
