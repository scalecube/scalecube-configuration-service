package io.scalecube.configuration.authorization;

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