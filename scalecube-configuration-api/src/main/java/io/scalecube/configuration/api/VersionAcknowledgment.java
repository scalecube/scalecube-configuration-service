package io.scalecube.configuration.api;

public class VersionAcknowledgment extends Acknowledgment {
  private static final Integer DEFAULT_VERSION = 1;

  private final Integer version;

  public VersionAcknowledgment(Integer version) {
    this.version = version;
  }

  public VersionAcknowledgment() {
    version = DEFAULT_VERSION;
  }

  @Override
  public String toString() {
    return "VersionAcknowledgment{" + "version=" + version + '}';
  }
}
