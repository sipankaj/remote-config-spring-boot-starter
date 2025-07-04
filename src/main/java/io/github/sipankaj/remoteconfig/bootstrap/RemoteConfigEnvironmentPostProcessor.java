package io.github.sipankaj.remoteconfig.bootstrap;

import com.google.cloud.secretmanager.v1.*;
import io.github.sipankaj.remoteconfig.RemoteConfigType;
import io.github.sipankaj.remoteconfig.loader.implementation.GcsConfigLoader;
import io.github.sipankaj.remoteconfig.loader.implementation.SftpConfigLoader;
import io.github.sipankaj.remoteconfig.model.GcsConfigProperties;
import io.github.sipankaj.remoteconfig.model.SftpConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import io.github.sipankaj.remoteconfig.annotation.EnableRemoteConfig;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;
import org.springframework.vault.config.EnvironmentVaultConfiguration;

import java.util.Objects;

public class RemoteConfigEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication application) {
        System.out.println("[RemoteConfig] Starting remote config loading...");

        boolean enabled = application.getAllSources().stream().anyMatch(src ->
                (src instanceof Class<?> clazz) && clazz.isAnnotationPresent(EnableRemoteConfig.class)
        );

        if (!enabled) {
            System.out.println("[RemoteConfig] Skipping remote config loading (annotation not present)");
            return;
        }

        String typeStr = env.getProperty("REMOTE_CONFIG_TYPE", "SFTP").toUpperCase();
        RemoteConfigType type;
        try {
            type = RemoteConfigType.valueOf(typeStr);
        } catch (IllegalArgumentException ex) {
            System.out.println("[RemoteConfig] Invalid REMOTE_CONFIG_TYPE: " + typeStr + ". Skipping.");
            return;
        }

        boolean loaded = false;

        try {
            switch (type) {
                case SFTP -> {
                    String host = env.getProperty("REMOTE_CONFIG_BUCKET_OR_HOST");
                    String file = env.getProperty("REMOTE_CONFIG_FILE");
                    int port = Integer.parseInt(env.getProperty("REMOTE_CONFIG_PORT", "22"));
                    String user = env.getProperty("REMOTE_CONFIG_USERNAME");

                    String source = env.getProperty("REMOTE_CONFIG_CREDENTIAL_SOURCE", "ENV").toUpperCase();
                    String pass = null, keyPath = null, keyPhrase = null;

                    switch (source) {
                        case "ENV" -> {
                            pass = env.getProperty("REMOTE_CONFIG_PASSWORD");
                            keyPath = env.getProperty("REMOTE_CONFIG_PRIVATE_KEY_PATH");
                            keyPhrase = env.getProperty("REMOTE_CONFIG_PRIVATE_KEY_PASSPHRASE");
                        }

                        case "GCP" -> {
                            String projectId = env.getProperty("REMOTE_CONFIG_GCP_PROJECT");
                            pass = readGcpSecret(projectId, "sftp-password");
                            keyPath = readGcpSecret(projectId, "sftp-private-key-path");
                            keyPhrase = readGcpSecret(projectId, "sftp-private-key-passphrase");
                        }

                        case "VAULT" -> {
                            String vaultPath = env.getProperty("REMOTE_CONFIG_VAULT_PATH", "secret/data/remote-config");
                            VaultTemplate vault = createVaultTemplate(env);
                            VaultResponse resp = vault.read(vaultPath);
                            if (resp != null && resp.getData() != null) {
                                pass = (String) resp.getData().get("sftp-password");
                                keyPath = (String) resp.getData().get("sftp-private-key-path");
                                keyPhrase = (String) resp.getData().get("sftp-private-key-passphrase");
                            }
                        }

                        default -> {
                            System.out.println("[RemoteConfig:SFTP] Unknown credential source: " + source);
                            return;
                        }
                    }

                    if (host == null || file == null || user == null || (pass == null && keyPath == null)) {
                        System.out.println("[RemoteConfig:SFTP] Missing required properties. Skipping.");
                        return;
                    }

                    var props = new SftpConfigProperties(host, port, user, pass, keyPath, keyPhrase, file);
                    new SftpConfigLoader(props, env).load();
                    loaded = true;
                }

                case GCS -> {
                    String bucket = env.getProperty("REMOTE_CONFIG_BUCKET_OR_HOST");
                    String file = env.getProperty("REMOTE_CONFIG_FILE");
                    if (bucket == null || file == null) {
                        System.out.println("[RemoteConfig:GCS] Missing bucket or file. Skipping.");
                        return;
                    }

                    var props = new GcsConfigProperties(bucket, file);
                    new GcsConfigLoader(props, env).load();
                    loaded = true;
                }
            }

        } catch (Exception e) {
            System.err.println("[RemoteConfig] Remote config failed: " + e.getMessage());
        }

        if (!loaded) {
            System.out.println("[RemoteConfig] Fallback to local application.yml");
        }
    }

    private String readGcpSecret(String projectId, String secretId) {
        try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
            SecretVersionName secretVersion = SecretVersionName.of(projectId, secretId, "latest");
            AccessSecretVersionResponse response = client.accessSecretVersion(secretVersion);
            return response.getPayload().getData().toStringUtf8();
        } catch (Exception e) {
            System.err.println("[RemoteConfig:GCP] Failed to fetch secret: " + secretId);
            return null;
        }
    }

    private VaultTemplate createVaultTemplate(ConfigurableEnvironment env) {
        EnvironmentVaultConfiguration config = new EnvironmentVaultConfiguration() {
            @Override
            public String vaultToken() {
                return Objects.requireNonNull(env.getProperty("VAULT_TOKEN"), "VAULT_TOKEN is missing");
            }

            @Override
            public String vaultEndpoint() {
                return Objects.requireNonNull(env.getProperty("VAULT_ADDR"), "VAULT_ADDR is missing");
            }
        };
        return new VaultTemplate(config.vaultEndpointProvider(), new TokenAuthentication(config.vaultToken()));
    }
}
