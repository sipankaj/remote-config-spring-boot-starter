# Remote Config Spring Boot Starter

A Spring Boot starter to load configuration files from remote sources (GCS or SFTP) during application bootstrap, before your main configuration is loaded.

## What is this library?

This library allows your Spring Boot application to fetch configuration files (YAML or properties) from remote locations such as Google Cloud Storage (GCS) or SFTP servers. It injects these remote configurations into the Spring Environment very early in the startup process, making them available as if they were local configuration files.

## Use Cases

- Centralized configuration management for multiple deployments.
- Securely store sensitive configuration outside the application package.
- Dynamically update configuration without rebuilding or redeploying the application.
- Support for cloud-native and containerized environments.

## How to Use

### 1. Add Dependency

Add the following to your `pom.xml`:

```xml
<dependency>
    <groupId>com.sipankaj</groupId>
    <artifactId>remote-config-spring-boot-starter</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```
### 2. Enable Remote Config
Annotate your main Spring Boot application class:

```java
import annotation.io.github.sipankaj.remoteconfig.EnableRemoteConfig;

@EnableRemoteConfig
@SpringBootApplication
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```
### 3. Provide Remote Config Properties
Set the following environment variables (recommended for early loading):

For SFTP
- REMOTE_CONFIG_TYPE=SFTP
- REMOTE_CONFIG_BUCKET_OR_HOST=<sftp-host>
- REMOTE_CONFIG_PORT=<sftp-port>
- REMOTE_CONFIG_USERNAME=<username>
- REMOTE_CONFIG_PASSWORD=<password> or REMOTE_CONFIG_PRIVATE_KEY_PATH=<path> (and optionally REMOTE_CONFIG_PRIVATE_KEY_PASSPHRASE=<passphrase>)
- REMOTE_CONFIG_FILE=<remote-config-file-path>

For GCS
- REMOTE_CONFIG_TYPE=GCS
- REMOTE_CONFIG_BUCKET_OR_HOST=<gcs-bucket>
- REMOTE_CONFIG_FILE=<gcs-file-path>


### 4. How it works
- The RemoteConfigEnvironmentPostProcessor checks for the @EnableRemoteConfig annotation.
- It reads configuration from environment variables.
- It loads the remote file and injects its properties into the Spring Environment before the main application configuration is processed.

### 5. UML Diagram

![UML Diagram](/docs/remoteconfig.png)

### 6. Notes
Use environment variables for remote config properties to ensure they are available before Spring loads local configuration.
For debugging early in the lifecycle, use System.out.println as logging may not be initialized.
Supports both YAML and properties files.