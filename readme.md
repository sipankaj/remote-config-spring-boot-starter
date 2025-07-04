
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

## ⚙️ How to Use

### 1️⃣ Add Dependency

```xml
<dependency>
  <groupId>com.sipankaj</groupId>
  <artifactId>remote-config-spring-boot-starter</artifactId>
  <version>1.0-SNAPSHOT</version>
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

| Variable                               | Description                    |
| -------------------------------------- | ------------------------------ |
| `REMOTE_CONFIG_TYPE`                   | Must be `SFTP`                 |
| `REMOTE_CONFIG_BUCKET_OR_HOST`         | Hostname of the SFTP server    |
| `REMOTE_CONFIG_PORT`                   | SFTP port (usually `22`)       |
| `REMOTE_CONFIG_USERNAME`               | SFTP username                  |
| `REMOTE_CONFIG_PASSWORD`               | (Optional) SFTP password       |
| `REMOTE_CONFIG_PRIVATE_KEY_PATH`       | (Optional) Path to private key |
| `REMOTE_CONFIG_PRIVATE_KEY_PASSPHRASE` | (Optional) Passphrase for key  |
| `REMOTE_CONFIG_FILE`                   | Path to remote config file     |

#### ☁️ For GCS:

| Variable                         | Description                       |
| -------------------------------- | --------------------------------- |
| `REMOTE_CONFIG_TYPE`             | Must be `GCS`                     |
| `REMOTE_CONFIG_BUCKET_OR_HOST`   | GCS bucket name                   |
| `REMOTE_CONFIG_FILE`             | Path to config file in the bucket |
| `GOOGLE_APPLICATION_CREDENTIALS` | Path to GCP service account key   |

> ✅ Use environment variables instead of `application.yml` to ensure early loading.

---

### 4️⃣ How it Works

1. `RemoteConfigEnvironmentPostProcessor` is triggered very early in Spring's bootstrap phase.
2. It checks for the `@EnableRemoteConfig` annotation.
3. Reads environment variables to detect remote config type and location.
4. Loads the remote file using GCS SDK or SFTP.
5. Parses the file as YAML/properties and adds it to the Spring `Environment`.

---

### 5️⃣ UML Diagram

![UML Diagram](/docs/remoteconfig.png)

---

## 📌 Notes

* 🧪 Use `System.out.println` for logging/debugging during bootstrap (as `Logger` may not be ready).
* ☁️ Supports both `.yml` and `.properties` formats.
* 💡 Best practice: provide fallback local config in case remote config fails.
* 🔐 Credentials should be injected securely via environment or secret managers.

---

## 📜 License

This project is licensed under the **MIT License**.

---

## 🙌 Contributions

PRs and issues welcome!
Future support for:

* AWS S3
* Azure Blob Storage

---

> Built with care by [@sipankaj](https://github.com/sipankaj) and powered by Spring Boot 💛