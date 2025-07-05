# 🌍 Remote Config Spring Boot Starter

A Spring Boot 3.x **starter library** that loads configuration files from **remote sources** such as **Google Cloud Storage (GCS)** or **SFTP**, injecting them into the Spring `Environment` **before local configuration is loaded**.

---

## 📖 What is this library?

This library lets your Spring Boot application securely and dynamically load `.yml` or `.properties` configuration files from remote locations **during startup**, enabling:

- Centralized config management
- Externalized secrets
- Environment-specific configuration
- CI/CD-driven dynamic configuration injection

---

## ✅ Use Cases

- 🔐 Securely store config/secrets outside the packaged app
- 🚀 Dynamically inject config in Kubernetes, Docker, or cloud platforms
- 🔁 Reuse config across environments without duplication
- 🧩 Simplify CI/CD pipelines with remote config injection

---

## 🚀 Prerequisites
- Java 17 or higher
- Spring Boot 3.x
- Maven 3.6+ (for dependency management)
- Network access to external secret managers (if used)

## ⚙️ How to Use

### 1️⃣ Add Dependency

```xml
<dependency>
  <groupId>com.sipankaj</groupId>
  <artifactId>remote-config-spring-boot-starter</artifactId>
  <version>1.0.0</version>
</dependency>
````

---

### 2️⃣ Enable Remote Config

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

---

### 3️⃣ Provide Remote Config Properties

Set the following **environment variables** (required for early loading):

#### 🔒 For SFTP:

| Variable                               | Description                        |
| -------------------------------------- | ---------------------------------- |
| `REMOTE_CONFIG_TYPE`                   | Must be `SFTP`                     |
| `REMOTE_CONFIG_BUCKET_OR_HOST`         | Hostname of the SFTP server        |
| `REMOTE_CONFIG_PORT`                   | SFTP port (usually `22`)           |
| `REMOTE_CONFIG_USERNAME`               | SFTP username                      |
| `REMOTE_CONFIG_FILE`                   | Path to remote config file         |
| `REMOTE_CONFIG_CREDENTIAL_SOURCE`      | `ENV` (default), `GCP`, or `VAULT` |
| *(only for ENV source)*                |                                    |
| `REMOTE_CONFIG_PASSWORD`               | (Optional) SFTP password           |
| `REMOTE_CONFIG_PRIVATE_KEY_PATH`       | (Optional) Path to private key     |
| `REMOTE_CONFIG_PRIVATE_KEY_PASSPHRASE` | (Optional) Passphrase for key      |

#### ☁️ For GCS:

| Variable                         | Description                       |
| -------------------------------- | --------------------------------- |
| `REMOTE_CONFIG_TYPE`             | Must be `GCS`                     |
| `REMOTE_CONFIG_BUCKET_OR_HOST`   | GCS bucket name                   |
| `REMOTE_CONFIG_FILE`             | Path to config file in the bucket |
| `GOOGLE_APPLICATION_CREDENTIALS` | Path to GCP service account key   |

✅ Use environment variables instead of `application.yml` to ensure early loading.

---

### 🔐 Credential Management for SFTP

The library now supports **multiple ways** to fetch credentials securely.

#### Option 1: ENV (default)

```env
REMOTE_CONFIG_CREDENTIAL_SOURCE=ENV
REMOTE_CONFIG_PASSWORD=mypassword
REMOTE_CONFIG_PRIVATE_KEY_PATH=/secrets/id_rsa
REMOTE_CONFIG_PRIVATE_KEY_PASSPHRASE=passphrase
```

#### Option 2: GCP Secret Manager

```env
REMOTE_CONFIG_CREDENTIAL_SOURCE=GCP
REMOTE_CONFIG_GCP_PROJECT=my-gcp-project-id
GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account.json
REMOTE_CONFIG_PASSWORD_SECRET=sftp-password
REMOTE_CONFIG_PRIVATE_KEY_PATH_SECRET=sftp-private-key-path
REMOTE_CONFIG_PRIVATE_KEY_PASSPHRASE_SECRET=sftp-private-key-passphrase
```

#### Option 3: HashiCorp Vault

```env
REMOTE_CONFIG_CREDENTIAL_SOURCE=VAULT
REMOTE_CONFIG_VAULT_PATH=secret/data/remote-config
VAULT_ADDR=https://vault.example.com
VAULT_TOKEN=s.xxxxxx
REMOTE_CONFIG_PASSWORD_SECRET=sftp-password
REMOTE_CONFIG_PRIVATE_KEY_PATH_SECRET=sftp-private-key-path
REMOTE_CONFIG_PRIVATE_KEY_PASSPHRASE_SECRET=sftp-private-key-passphrase
```


ℹ️ The fallback order is: `REMOTE_CONFIG_CREDENTIAL_SOURCE` → `ENV`

---

### 4️⃣ How it Works

1. `RemoteConfigEnvironmentPostProcessor` is triggered **early in Spring bootstrap**.
2. It checks for the `@EnableRemoteConfig` annotation.
3. Reads environment variables to detect remote config type and location.
4. Loads the remote file using either **GCS** or **SFTP**.
5. Parses the config as `.yml` or `.properties`.
6. Adds it to the **Spring Environment** before any `application.yml` is processed.

---

### 5️⃣ UML Diagram

![UML Diagram](/docs/remoteconfig.png)

---

## 📌 Notes

* 🧪 Use `System.out.println` for debugging during early bootstrap (logging not yet initialized).
* ☁️ Supports both `.yml` and `.properties` formats.
* 🔁 Fallback to local config if remote fails.
* 🔐 Credentials should never be hardcoded.

---

## 📜 License

Licensed under the **MIT License**.

---

## 🙌 Contributions

PRs and issues welcome!
Upcoming ideas:

* AWS S3 support
* Azure Blob Storage
* Dynamic refresh support (e.g., via actuator)

---

> Built with care by [@sipankaj](https://github.com/sipankaj) and powered by Spring Boot 💛
