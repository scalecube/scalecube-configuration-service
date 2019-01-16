package io.scalecube.configuration;

import io.scalecube.config.ConfigRegistry;
import io.scalecube.config.ConfigRegistrySettings;
import io.scalecube.config.audit.Slf4JConfigEventListener;
import io.scalecube.config.source.ClassPathConfigSource;
import io.scalecube.config.source.SystemEnvironmentConfigSource;
import io.scalecube.config.source.SystemPropertiesConfigSource;
import io.scalecube.config.vault.VaultConfigSource;
import java.util.Optional;

/** Configures the ConfigRegistry with sources. */
public final class ConfigRegistryConfiguration {

  private static final int RELOAD_INTERVAL_SEC = 300;
  private static final String VAULT_ADDR_PROP_KEY = "VAULT_ADDR";
  private static final String VAULT_TOKEN_PROP_KEY = "VAULT_TOKEN";
  private static final String VAULT_SECRETS_PATH_PROP_KEY = "VAULT_SECRETS_PATH";
  private static final String VAULT = "vault";
  private static final String SYS_PROP = "sys_prop";
  private static final String ENV_VAR = "env_var";
  private static final String CLASSPATH = "classpath";
  private static final String PROPS_FILE_EXT = ".props";

  private static ConfigRegistry configRegistry;

  /**
   * Builds a ConfigRegistry.
   *
   * @return ConfigRegistry
   */
  public static ConfigRegistry configRegistry() {
    if (configRegistry != null) {
      return configRegistry;
    }

    ConfigRegistrySettings.Builder builder =
        ConfigRegistrySettings.builder()
            .reloadIntervalSec(RELOAD_INTERVAL_SEC)
            .addListener(new Slf4JConfigEventListener())
            .addLastSource(SYS_PROP, new SystemPropertiesConfigSource())
            .addLastSource(ENV_VAR, new SystemEnvironmentConfigSource())
            .addLastSource(
                CLASSPATH,
                new ClassPathConfigSource(path -> path.toString().endsWith(PROPS_FILE_EXT)));

    VaultConfigSource vaultConfigSource = buildVaultConfigSource();

    if (vaultConfigSource != null) {
      builder.addLastSource(VAULT, vaultConfigSource);
    }

    configRegistry = ConfigRegistry.create(builder.build());
    return configRegistry;
  }

  private static VaultConfigSource buildVaultConfigSource() {
    String vaultAddr =
        Optional.ofNullable(System.getenv(VAULT_ADDR_PROP_KEY))
            .orElseGet(() -> System.getProperty(VAULT_ADDR_PROP_KEY, null));

    String vaultToken =
        Optional.ofNullable(System.getenv(VAULT_TOKEN_PROP_KEY))
            .orElseGet(() -> System.getProperty(VAULT_TOKEN_PROP_KEY, null));

    String vaultSecretsPath =
        Optional.ofNullable(System.getenv(VAULT_SECRETS_PATH_PROP_KEY))
            .orElseGet(() -> System.getProperty(VAULT_SECRETS_PATH_PROP_KEY, null));

    if (vaultAddr == null || vaultToken == null || vaultSecretsPath == null) {
      return null;
    }

    return VaultConfigSource.builder(vaultAddr, vaultToken, vaultSecretsPath).build();
  }
}
