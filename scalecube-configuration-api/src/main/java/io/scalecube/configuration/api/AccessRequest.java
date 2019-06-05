package io.scalecube.configuration.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface AccessRequest {

  String repository();

  @JsonProperty("APIKey")
  Object apiKey();
}
