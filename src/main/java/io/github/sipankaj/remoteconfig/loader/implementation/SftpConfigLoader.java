package io.github.sipankaj.remoteconfig.loader.implementation;

import io.github.sipankaj.remoteconfig.loader.interfaces.IRemoteConfigLoader;
import io.github.sipankaj.remoteconfig.model.SftpConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;

import java.io.InputStream;
import java.util.*;

@Slf4j
public class SftpConfigLoader implements IRemoteConfigLoader {

    private final SftpConfigProperties props;
    private final ConfigurableEnvironment environment;

    public SftpConfigLoader(SftpConfigProperties props, ConfigurableEnvironment environment) {
        this.props = Objects.requireNonNull(props, "SftpConfigProperties must not be null");
        this.environment = Objects.requireNonNull(environment, "Environment must not be null");
    }

    @Override
    public void load() throws Exception {
        System.out.println("[RemoteConfig:SFTP] Loading configuration from SFTP host: " + props.host() + " file: " + props.file());
        SessionFactory<?> sessionFactory = createSftpSessionFactory();

        try (Session<?> session = sessionFactory.getSession();
             InputStream inputStream = session.readRaw(props.file())) {

            IRemoteConfigLoader.applyToEnvironment(inputStream, props.file(), environment);
        }
    }

    private SessionFactory<?> createSftpSessionFactory() {
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);
        factory.setHost(props.host());
        factory.setPort(props.port());
        factory.setUser(props.username());
        factory.setAllowUnknownKeys(true);

        if (props.privateKeyPath()!= null && !props.privateKeyPath().isBlank()) {
            factory.setPrivateKey(new FileSystemResource(props.privateKeyPath()));
            if (props.privateKeyPassphrase() != null && !props.privateKeyPassphrase().isBlank()) {
                factory.setPrivateKeyPassphrase(props.privateKeyPassphrase());
            }
        } else if (props.password() != null && !props.password().isBlank()) {
            factory.setPassword(props.password());
        } else {
            throw new IllegalArgumentException("Either privateKeyPath or password must be provided for SFTP.");
        }

        return factory;
    }

}
