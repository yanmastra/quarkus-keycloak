# Changelog

All notable changes to the `io.yanmastra` extensions are documented here.
Versions follow [Semantic Versioning](https://semver.org/).

---

## [4.1.4] — 2026-05-19

### Fixed

#### `quarkus-base`
- **`KeyValueCacheUtils` — concurrent request safety** (`KeyValueCacheUtils.java`)
  - Added 32-stripe `ReentrantReadWriteLock` array to prevent `java.nio.channels.OverlappingFileLockException`
    when multiple threads in the same JVM access the same session cache file concurrently.
  - Root cause: `java.nio.channels.FileChannel.lock()` is designed for inter-process locking only.
    Within the same JVM, two threads calling `lock()` on the same file (even with shared read mode)
    throws `OverlappingFileLockException` immediately — it does not queue or wait.
  - **`findCache`** now acquires a JVM-level read lock (`ReadWriteLock.readLock()`) before calling
    the shared `FileChannel.lock()`, allowing multiple concurrent reads without conflict.
  - **`saveCache`** / **`removeCache`** now acquire a JVM-level write lock (`ReadWriteLock.writeLock()`),
    blocking all readers and other writers within the JVM before the exclusive `FileChannel.lock()`.
  - Striped design: 32 fixed lock objects (~1.6 KB total memory) ensure different sessions
    rarely contend; same session always maps to the same stripe and serializes safely.
  - `FileChannel.lock()` calls are preserved for cross-process (multi-instance) safety.

### Changed

- All extensions bumped from **4.1.3 → 4.1.4** to consume the `quarkus-base` fix:
  - `quarkus-authentication`
  - `quarkus-authorization`
  - `quarkus-microservices-common`
  - `media-file-manager`
- `quarkus-error-mail-notification` bumped from **4.1.2 → 4.1.4**.

### Migration

Update the version property in your consumer project's `pom.xml`:

```xml
<properties>
    <quarkus-keycloak.version>4.1.4</quarkus-keycloak.version>
</properties>
```

No API changes — this is a drop-in replacement.

---

## [4.1.3] — previous release

- Error message improvements in authentication flows.
- Version alignment across all extensions.

## [4.1.2] — previous release

- Initial release of `quarkus-error-mail-notification`.
- Various bug fixes across authentication and microservices-common.

## [4.1.1] — previous release

- Stability improvements and dependency updates.

## [4.1.0] — previous release

- Initial 4.1.x release series.
