package io.github.sipankaj.remoteconfig.bootstrap;

import io.github.sipankaj.remoteconfig.RemoteConfigType;
import io.github.sipankaj.remoteconfig.loader.implementation.GcsConfigLoader;
import io.github.sipankaj.remoteconfig.loader.implementation.SftpConfigLoader;
import io.github.sipankaj.remoteconfig.model.GcsConfigProperties;
import io.github.sipankaj.remoteconfig.model.SftpConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import io.github.sipankaj.remoteconfig.annotation.EnableRemoteConfig;

public class RemoteConfigEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication application) {
        System.out.println("[RemoteConfig] Starting remote config loading...");
        boolean enabled = application.getAllSources().stream().anyMatch(src -> {
            if (src instanceof Class<?> clazz) {
                return clazz.isAnnotationPresent(EnableRemoteConfig.class);
            }
            return false;
        });
        if (!enabled) {
            System.out.println("[RemoteConfig] Skipping remote config loading (annotation not present)");
            return;
        }

        String typeStr = env.getProperty("REMOTE_CONFIG_TYPE", "SFTP").toUpperCase();

        RemoteConfigType type;
        try {
            type = RemoteConfigType.valueOf(typeStr);
        } catch (IllegalArgumentException ex) {
            System.out.println("[RemoteConfig] Invalid REMOTE_CONFIG_TYPE: " + typeStr + ". Skipping remote config.");
            return;
        }

        boolean loaded = false;

        try {
            switch (type) {
                case SFTP -> {
                    String host = env.getProperty("REMOTE_CONFIG_BUCKET_OR_HOST");
                    String file = env.getProperty("REMOTE_CONFIG_FILE");
                    String user = env.getProperty("REMOTE_CONFIG_USERNAME");
                    String pass = env.getProperty("REMOTE_CONFIG_PASSWORD");
                    String keyPath = env.getProperty("REMOTE_CONFIG_PRIVATE_KEY_PATH");
                    String keyPhrase = env.getProperty("REMOTE_CONFIG_PRIVATE_KEY_PATH");
                    int port = Integer.parseInt(env.getProperty("REMOTE_CONFIG_PORT", "22"));

                    if (host == null || file == null || user == null || (pass == null && keyPath == null)) {
                        System.out.println("[RemoteConfig:SFTP] Missing required properties. Skipping remote config.");
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
                        System.out.println("[RemoteConfig:GCS] Missing bucket or file. Skipping remote config.");
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
}
