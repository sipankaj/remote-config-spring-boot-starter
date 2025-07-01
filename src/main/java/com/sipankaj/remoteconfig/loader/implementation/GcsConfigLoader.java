package com.sipankaj.remoteconfig.loader.implementation;

import com.google.cloud.storage.*;
import com.sipankaj.remoteconfig.loader.interfaces.IRemoteConfigLoader;
import com.sipankaj.remoteconfig.model.GcsConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

public class GcsConfigLoader implements IRemoteConfigLoader {

    private final GcsConfigProperties config;
    private final ConfigurableEnvironment environment;

    public GcsConfigLoader(GcsConfigProperties config, ConfigurableEnvironment environment) {
        this.config = Objects.requireNonNull(config);
        this.environment = Objects.requireNonNull(environment);
    }

    @Override
    public void load() throws Exception {
        System.out.println("[RemoteConfig:GCS] Loading configuration from GCS bucket: " + config.bucket() + " file: " + config.file());
        Storage storage = StorageOptions.getDefaultInstance().getService();

        Blob blob = storage.get(config.bucket(), config.file());
        if (blob == null || !blob.exists()) {
            throw new IllegalArgumentException("GCS file not found: " + config.bucket() + "/" + config.file());
        }

        try (InputStream is = new ByteArrayInputStream(blob.getContent())) {
            IRemoteConfigLoader.applyToEnvironment(is, config.file(), environment);
        }
    }
}
