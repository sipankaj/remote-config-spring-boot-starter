package io.github.sipankaj.remoteconfig.model;

import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.boot.context.properties.ConfigurationProperties;

public record GcsConfigProperties(
        String bucket,
        String file
) {}