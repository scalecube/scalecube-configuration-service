package io.scalecube.configuration;

import com.bettercloud.vault.EnvironmentLoader;
import io.scalecube.config.ConfigRegistry;
import io.scalecube.config.ConfigRegistrySettings;
import io.scalecube.config.ConfigRegistrySettings.Builder;
import io.scalecube.config.audit.Slf4JConfigEventListener;
import io.scalecube.config.source.ClassPathConfigSource;
import io.scalecube.config.source.SystemEnvironmentConfigSource;
import io.scalecube.config.source.SystemPropertiesConfigSource;
import io.scalecube.config.vault.VaultConfigSource;

/** Configures the ConfigRegistry with sources. */
public class ConfigRegistryConfiguration {
  private static final int RELOAD_INTERVAL_SEC = 300;
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
            .addLastSource("sys_prop", new SystemPropertiesConfigSource())
            .addLastSource("env_var", new SystemEnvironmentConfigSource())
            .addLastSource(
                "classpath", new ClassPathConfigSource(path -> path.toString().endsWith(".props")));

    // for test purposes without vault access
    if (System.getenv().get("VAULT_ADDR") != null) {
      builder.addLastSource("vault", VaultConfigSource.builder().build());
      addVaultApiKeysPath(builder);
    }

    configRegistry = ConfigRegistry.create(builder.build());
    return configRegistry;
  }

  private static void addVaultApiKeysPath(Builder builder) {
    EnvironmentLoader environmentLoader = new EnvironmentLoader();
    String adiKeysSecretPathPattern = AppSettings.builder()
        .build().getProperty("vault.secret.path");
    builder.addLastSource("vault-api-keys",
        VaultConfigSource
            .builder(
                environmentLoader.loadVariable("VAULT_ADDR"),
                environmentLoader.loadVariable("VAULT_TOKEN"),
                String.format(
                    adiKeysSecretPathPattern,
                    environmentLoader.loadVariable("VAULT_SECRETS_PATH"))
                )
            .build());
  }
}
