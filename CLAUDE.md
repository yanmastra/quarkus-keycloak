# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A Quarkus-based microservices learning project with Keycloak integration. The repo is a **mono-repo of independent Maven modules** (no root pom.xml). It contains reusable Quarkus extensions in `extensions/` and sample services in `services/`.

**Java version:** 21
**Platform:** Developed/tested on macOS

## Build Commands

### Building extensions

Each extension is built independently. From the repo root:
```sh
./scripts/deploy-dependency.sh   # Builds all extensions and copies artifacts to deployed-dependencies/
```

Or build a single extension manually:
```sh
cd extensions/quarkus-authentication
mvn clean install -DskipTests
```

Build order matters — `quarkus-base` must be built first, then the extensions that depend on it:
1. `quarkus-base`
2. `quarkus-authentication`, `quarkus-authorization` (depend on quarkus-base)
3. `quarkus-microservices-common` (depends on quarkus-base)

### Building and running services

```sh
cd services/rest-sample
mvn quarkus:dev                    # Dev mode with hot reload (port 4001)
```

```sh
cd services/media-mail-service
mvn quarkus:dev                    # Dev mode (port 4002)
```

### Running tests

```sh
cd services/rest-sample
mvn test                           # Unit tests
mvn verify                         # Unit + integration tests (IT tests skipped by default)
mvn verify -DskipITs=false         # Force run integration tests
```

Same pattern applies to extension integration tests:
```sh
cd extensions/quarkus-authentication/integration-tests
mvn test
```

## Architecture

### Extension pattern (extensions/)

Extensions follow the **Quarkus extension structure** with `deployment/` + `runtime/` modules under a parent POM. `quarkus-base` is a plain library (no deployment module).

- **quarkus-base** — Utility classes (caching, password generation) shared across all extensions
- **quarkus-authentication** — JWT generation/validation, cookie sessions, bearer token auth. **Cannot** be combined with quarkus-authorization (conflicting auth beans)
- **quarkus-authorization** — Keycloak-specific bearer token validation and policy enforcement
- **quarkus-microservices-common** — Auto-generates 5 CRUD REST endpoints per entity. Converts query params to HQL for filtering, sorting, and pagination. Key base classes: `BaseEntity`, `BaseDto`, `BaseRepository`
- **media-file-manager** — (Planned) Media file management extension, currently incomplete

### Services pattern (services/)

`services/pom.xml` is the parent POM aggregating services. Each service:
- Uses quarkus-authorization + quarkus-microservices-common for Keycloak auth and CRUD
- Connects to PostgreSQL via Hibernate ORM with Panache
- Exposes OpenAPI/Swagger UI
- Runs on a distinct port (4001, 4002, etc.)

### Infrastructure (infra/)

Docker Compose provides PostgreSQL, Keycloak, and Nginx. Requires local DNS setup (see `docs/how-to-setup.md`):
```sh
# Start infrastructure
cd infra/docker && docker compose up -d
```

Keycloak realm config is imported from `infra/docker/keycloak/imports/realm-sample.json`.

Production-style Keycloak setup is in `infra/keycloak-setup/`.

### Package namespace

All Java code uses the `io.yanmastra` root package:
- `io.yanmastra.quarkusBase` — base utilities
- `io.yanmastra.authentication` — auth extension
- `io.yanmastra.authorization` — authz extension
- `io.yanmastra.quarkus.microservices.common` — CRUD framework
- `io.yanmastra.microservices.*` — individual services

## Environment Setup

1. Copy `.env.example` to `.env` and configure database, Keycloak, and domain variables
2. Set up local DNS entries in `/etc/hosts` pointing `10.123.123.123` to your chosen domain
3. Run `./infra/docker/create-certificate.sh` to generate SSL certs for Keycloak
4. Start infrastructure with Docker Compose
5. Configure Keycloak realm (see `docs/keycloak-setup.md`)
