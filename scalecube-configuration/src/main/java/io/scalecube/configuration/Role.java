package io.scalecube.configuration;

public enum Role {
  Owner("owner"),
  Admin("admin"),
  Member("member");

  private final String role;

  Role(String role) {
    this.role = role;
  }

  String value() {
    return role;
  }
}