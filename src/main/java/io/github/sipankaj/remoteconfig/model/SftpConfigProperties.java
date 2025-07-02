package io.github.sipankaj.remoteconfig.model;

public record SftpConfigProperties(
        String host,
        int port,
        String username,
        String password,
        String privateKeyPath,
        String privateKeyPassphrase,
        String file
) {}
