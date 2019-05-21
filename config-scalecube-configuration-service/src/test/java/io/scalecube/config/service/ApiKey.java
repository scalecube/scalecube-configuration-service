package io.scalecube.config.service;

import java.util.Arrays;
import java.util.Objects;

public class ApiKey {

  private String APIKey;
  private String[] Permissions;

  public ApiKey() {}

  public ApiKey(String apiKey, String[] permissions) {
    this.APIKey = apiKey;
    this.Permissions = permissions;
  }

  /** @return the apiKey */
  public String getApiKey() {
    return this.APIKey;
  }

  /** @return the permissions */
  public String[] getPermissions() {
    return this.Permissions;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(this.Permissions);
    result = prime * result + Objects.hash(APIKey);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ApiKey)) {
      return false;
    }
    ApiKey other = (ApiKey) obj;
    return Objects.equals(APIKey, other.APIKey) && Arrays.equals(Permissions, other.Permissions);
  }
}
