# Practicing to build Microservices with Quarkus, Quarkus-extension, Keycloak, and SSL integrated
This project was created to practice building Microservices with Quarkus, Securing services with Keycloak and testing the performance of hibernate-orm and hibernate-reactive.
In this project, there is a sample of the Rest-API project that contains many samples of Endpoint classes and samples of hibernate queries, etc.
If you are interested, let's see a couple of documentation below.

<hr/>

## Folder Structure
```
root/
├── extensions/           # Quarkus extensions and shared libraries
│   ├── quarkus-base/
│   ├── quarkus-authentication/
│   ├── quarkus-authorization/
│   ├── quarkus-microservices-common/
│   ├── quarkus-error-mail-notification/
│   └── media-file-manager/
├── services/             # Microservices
│   ├── rest-sample/
│   └── media-mail-service/
├── infra/                # Infrastructure configs
│   ├── docker/           # Docker Compose, Keycloak, Nginx, PostgreSQL
│   └── keycloak-setup/   # Production Keycloak setup
├── scripts/              # Build & utility scripts
│   ├── deploy-dependency.sh
│   ├── clean-dependencies.sh
│   ├── quarkus-create-app.sh
│   └── quarkus-create-extension.sh
├── deployed-dependencies/ # Built extension artifacts
└── docs/                 # Documentation
```

## Table of contents
- [Introduction](/docs/intro.md)
- [Prerequisites](#prerequisites)
- [How to use extensions?](/docs/how-to-use-dependencies.md)
- [How to set up for extension development?](/docs/how-to-setup.md)
- [~~How to Create a Project ?~~](/docs/how-to-create-project.md)
- [~~How to run a service ?~~](/docs/how-to-run-a-services.md)
- [~~Postman Collection file~~](/docs/postman/)
- [~~Architecture Study Case~~](/docs/architecture-study-case.md)
- [~~Performance Test~~](/docs/performance-test)

<hr/>

## Using Extensions as Dependencies

The extensions in this project are published to **GitHub Packages**. You can use them in your own Quarkus project without cloning this repo.

### 1. Create a GitHub Personal Access Token (PAT)

You need a Personal Access Token (PAT) to authenticate with GitHub Packages.

1. Go to [GitHub Settings > Developer settings > Personal access tokens > Tokens (classic)](https://github.com/settings/tokens)
2. Click **"Generate new token"** > **"Generate new token (classic)"**
3. Give it a descriptive name (e.g. `maven-packages-read`)
4. Select the **`read:packages`** scope
5. Click **"Generate token"**
6. Copy the token immediately — you won't be able to see it again

### 2. Configure Maven settings

Add the following to your `~/.m2/settings.xml`:

```xml
<settings>
  <servers>
    <server>
      <id>github</id>
      <username>YOUR_GITHUB_USERNAME</username>
      <password>YOUR_GITHUB_PAT</password>
    </server>
  </servers>
</settings>
```

### 3. Add the repository to your project `pom.xml`

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/yanmastra/quarkus-keycloak</url>
    </repository>
</repositories>
```

### 4. Add the dependencies you need

```xml
<!-- Base utilities (required by all other extensions) -->
<dependency>
    <groupId>io.yanmastra</groupId>
    <artifactId>quarkus-base</artifactId>
    <version>4.1.0</version>
</dependency>

<!-- Keycloak authorization (bearer token + policy enforcement) -->
<dependency>
    <groupId>io.yanmastra</groupId>
    <artifactId>quarkus-authorization</artifactId>
    <version>4.1.0</version>
</dependency>

<!-- JWT authentication (cannot be combined with quarkus-authorization) -->
<dependency>
    <groupId>io.yanmastra</groupId>
    <artifactId>quarkus-authentication</artifactId>
    <version>4.1.0</version>
</dependency>

<!-- Auto-generated CRUD REST endpoints -->
<dependency>
    <groupId>io.yanmastra</groupId>
    <artifactId>quarkus-microservices-common</artifactId>
    <version>4.1.0</version>
</dependency>

<!-- Error mail notification -->
<dependency>
    <groupId>io.yanmastra</groupId>
    <artifactId>quarkus-error-mail-notification</artifactId>
    <version>4.1.0</version>
</dependency>
```

> **Note:** `quarkus-authentication` and `quarkus-authorization` have conflicting auth beans and cannot be used together in the same service.

<hr/>

## Prerequisites
To follow this guide, you need:
1. Understand Java
2. Understand Object Oriented Programming
3. Understand Docker
4. OpenJDK 21+ installed
5. Apache Maven 3.9.6 or newer
6. Docker

> :warning: Recommended to use ``sdkman`` to install OpenJDK or GraalVM on your device
> <br/> [See this to install ``SDKMAN`` ](https://sdkman.io/install)
