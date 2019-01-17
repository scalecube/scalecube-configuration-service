package io.scalecube.configuration;

import io.scalecube.config.ConfigRegistry;
import io.scalecube.config.ConfigRegistrySettings;
import io.scalecube.config.audit.Slf4JConfigEventListener;
import io.scalecube.config.source.ClassPathConfigSource;
import io.scalecube.config.source.SystemEnvironmentConfigSource;
import io.scalecube.config.source.SystemPropertiesConfigSource;
import io.scalecube.config.vault.VaultConfigSource;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/** Configures the ConfigRegistry with sources. */
public final class AppConfiguration {

  private static final int RELOAD_INTERVAL_SEC = 300;
  private static final Pattern CONFIG_PATTERN = Pattern.compile("(.*)\\.config\\.properties");
  private static final Predicate<Path> PATH_PREDICATE =
      path -> CONFIG_PATTERN.matcher(path.toString()).matches();

  private static final String VAULT_ADDR_PROP_KEY = "VAULT_ADDR";
  private static final String VAULT_TOKEN_PROP_KEY = "VAULT_TOKEN";
  private static final String VAULT_SECRETS_PATH_PROP_KEY = "VAULT_SECRETS_PATH";

  private static final ConfigRegistry configRegistry;

  static {
    ConfigRegistrySettings.Builder builder =
        ConfigRegistrySettings.builder()
            .reloadIntervalSec(RELOAD_INTERVAL_SEC)
            .addListener(new Slf4JConfigEventListener());

    VaultConfigSource vaultConfigSource = buildVaultConfigSource();

    if (vaultConfigSource != null) {
      builder.addLastSource("vault", vaultConfigSource);
    }
    builder.addLastSource("sys_prop", new SystemPropertiesConfigSource());
    builder.addLastSource("env_var", new SystemEnvironmentConfigSource());
    builder.addLastSource("cp", new ClassPathConfigSource(PATH_PREDICATE));

    configRegistry = ConfigRegistry.create(builder.build());
  }

  public static ConfigRegistry configRegistry() {
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
