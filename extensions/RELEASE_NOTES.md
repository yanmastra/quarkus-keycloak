# Release Notes

## 4.2.1 / quarkus-media-file-manager 1.0.1 — 2026-06-19

### quarkus-base
- Added `UserPermission` class (`io.yanmastra.quarkusBase.security.UserPermission`) extending `java.security.Permission`
  - Name-based `implies()`, `equals()`, `hashCode()`, and `getActions()`
  - Shared across authentication and authorization extensions

### quarkus-authentication
- Implemented `getPermissions()` on `UserSecurityIdentity` — maps user authorities to `UserPermission` objects
- Implemented `getPermissions()` on `UnauthorizedSecurityIdentity` — returns empty set

### quarkus-authorization
- Implemented `getPermissions()` on `UserSecurityIdentity` — maps Keycloak roles to `UserPermission` objects

### quarkus-microservices-common
- Fixed PostgreSQL JDBC URL in integration-tests (removed MySQL-specific `serverTimezone`/`timezone` params)
- Fixed `MediaType` import in integration test (`jakarta.ws.rs.core.MediaType`)

### quarkus-media-file-manager
- No functional changes — version aligned with quarkus-base dependency bump

---

## 4.2.0 — 2026-06-19

- Added `quarkus-media-file-manager` extension (v1.0.0)
  - Image upload with automatic width variants (320, 640px)
  - File upload for non-image types
  - Secured (authenticated) endpoints for both image and file storage
  - Local filesystem and S3/MinIO storage backends
  - Load by file ID + optional size variant
  - Delete by file ID using meta manifest
  - `specificLocation` support for custom path prefixes
- Upgraded all extensions to Quarkus 3.33.2 LTS
