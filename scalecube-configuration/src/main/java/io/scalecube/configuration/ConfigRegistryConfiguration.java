package io.scalecube.configuration;

import io.scalecube.config.ConfigRegistry;
import io.scalecube.config.ConfigRegistrySettings;
import io.scalecube.config.audit.Slf4JConfigEventListener;
import io.scalecube.config.source.ClassPathConfigSource;
import io.scalecube.config.source.SystemEnvironmentConfigSource;
import io.scalecube.config.source.SystemPropertiesConfigSource;
import io.scalecube.config.vault.VaultConfigSource;

/** Configures the ConfigRegistry with sources. */
public final class ConfigRegistryConfiguration {

  private static final int RELOAD_INTERVAL_SEC = 300;
  private static final String VAULT_ADDR_ENV_VAR = "VAULT_ADDR";
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
                CLASSPATH, new ClassPathConfigSource(path -> path.toString().endsWith(
                    PROPS_FILE_EXT)));

    // for test purposes without vault access
    boolean isVaultAccessible = System.getenv().get(VAULT_ADDR_ENV_VAR) != null;
    if (isVaultAccessible) {
      builder.addLastSource(VAULT, VaultConfigSource.builder().build());
    }

    configRegistry = ConfigRegistry.create(builder.build());
    return configRegistry;
  }
}
