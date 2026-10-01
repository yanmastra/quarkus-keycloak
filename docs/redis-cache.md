# Redis cache (`quarkus-redis-cache`)

`KeyValueCacheUtils` (in `quarkus-base`) keeps login sessions and cookie tokens. By default it writes encrypted files to
local disk. Add `quarkus-redis-cache` to a service to keep them in Redis instead. Nothing else changes: callers still use
`saveCache` / `findCache` / `removeCache`.

Services that don't add the extension get no Redis dependency and keep using local files.

## Enable

```xml
<dependency>
    <groupId>io.yanmastra</groupId>
    <artifactId>quarkus-redis-cache</artifactId>
    <version>4.3.0</version>
</dependency>
```

```properties
quarkus.redis.hosts=redis://:${REDIS_PASSWORD}@localhost:6379
```

Connection settings are the standard [Quarkus Redis client](https://quarkus.io/guides/redis) properties. In dev and test
mode, if `quarkus.redis.hosts` is not set, Quarkus Dev Services starts a Redis container automatically (needs Docker).

| Property | Default | Meaning |
|---|---|---|
| `cache.redis.enabled` | `true` | `false` = keep using local files and move data back from Redis (see below) |
| `cache.redis.key-prefix` | `kvcache` | Prefix of every Redis key. Each cache is a hash `<prefix>:<cacheName>`; `<prefix>:meta:*` is reserved |
| `cache.redis.encrypt` | `true` | AES-GCM encrypt values. The key is created by the first instance and stored in Redis, so all instances share it. Keep it the same across instances and restarts |

If Redis cannot be reached at start-up the application fails to start (it does not silently fall back to files, which
would split sessions between two stores). The exception is `cache.redis.enabled=false`: there the application starts with
local files and logs a warning that Redis data was not moved back.

## Local Redis for testing

```sh
cd infra/docker
docker compose up -d redis        # password: REDIS_PASSWORD from .env (default: redis_local_password)
```

## Migration

Both directions happen automatically at start-up.

**Files to Redis** — when the extension is added, every local cache file (`<cache_directory>/.cache_v2/.cache.*`) is
copied into Redis. Values that already exist in Redis are never overwritten. Each migrated file is renamed to
`.cache.<name>.migrated` and kept as a backup.

**Redis back to files** — the extension must still be present to read Redis, so do it in two steps:

1. Set `cache.redis.enabled=false` and start the application once, with the extension still on the classpath. All caches are
   moved from Redis into local files and removed from Redis.
2. Remove the dependency.

With several instances starting at the same time, only one runs a migration (Redis lock with a 2-minute expiry); the
others skip it. Note that local files belong to one instance, so when moving *back* to files the instance that runs
first receives the data.

## Using the cache from code that might run on the event loop

The Redis client cannot block an event-loop thread. Endpoints returning a plain object or `Response` run on worker threads
and can use the blocking methods. Endpoints returning `Uni` (or annotated `@NonBlocking`) must use the async ones:

| Blocking | Non-blocking |
|---|---|
| `KeyValueCacheUtils.saveCache/findCache/removeCache` | `saveCacheAsync/findCacheAsync/removeCacheAsync` |
| `AuthenticationService.createAccessToken` | `createAccessTokenAsync` |
| `AuthenticationService.checkSession / getUserId / removeSession / logout` | `checkSessionAsync / getUserIdAsync / removeSessionAsync / logoutAsync` |

Calling a blocking method on an event-loop thread fails with a message pointing to the async alternative.
Request authentication itself (`AuthenticationMechanism`) already uses the async path. If the cache is unreachable while
authenticating, the session cookie is ignored and the request falls back to the `Authorization` header.

This is not only about endpoints, and it is not limited to what this repo itself does. These extensions are consumed by
other projects we don't control: a consuming service may call the cache from its own service classes, from Qute template
rendering, from an `ErrorEvent` observer or `HtmlErrorMapper`, from a Vert.x route handler, or from anything else it
adds — in blocking or reactive style, as it chooses. The one fact that always decides whether a blocking cache call is
safe is **which thread is executing when it runs**, never what kind of class it's in:

- A worker thread (the default for an endpoint returning a plain object or `Response`, or for `@Blocking` code): the
  blocking methods are fine.
- The Vert.x event loop (`Uni`/`Multi`/`@NonBlocking` endpoints, `AuthenticationMechanism`, exception mapping that
  RESTEasy Reactive dispatches without a worker-thread hop — e.g. `ErrorMapper.asyncResponse` for an exception that
  happens outside a worker-dispatched request such as an auth failure — a Qute render triggered from any of the above,
  a raw Vert.x handler, or anything else running there): only the `*Async` methods are safe.

`ErrorMapper` itself and the one `ErrorEvent` observer shipped in this repo (`quarkus-error-mail-notification`'s
`ErrorMailNotifier`) don't call the cache, so there is nothing to fix in the extensions themselves. But both are
extension points (`ErrorEvent` observers, `HtmlErrorMapper` implementations, Qute data resolvers used by a consumer's own
templates) that a consuming project can hook into, and there is no way for us to know or enforce which thread that
consumer's code runs on. Calling a blocking method on the wrong thread fails loudly with a message pointing at the async
alternative — it does not corrupt data or hang — but there is nothing that stops a consumer from hitting it, so this is
worth calling out wherever the extension is documented to its consumers.

## Writing another storage

Implement `io.yanmastra.quarkusBase.cache.KeyValueCacheStore` and call `KeyValueCacheUtils.registerStore(store)` at start-up.
The migration to and from local files works for any implementation.

## Tests

```sh
cd extensions/quarkus-base && mvn test                        # file store, migration logic, async (no Docker needed)
cd extensions/quarkus-redis-cache/integration-tests && mvn test   # real Redis via Dev Services

# against infra/docker's compose Redis instead (see the `manual` profile in this module's application.properties)
docker compose -f infra/docker/docker-compose.yml up -d redis
mvn test -Dquarkus.test.profile=manual -DREDIS_PASSWORD=redis_local_password
```
