package io.yanmastra.quarkus.rediscache;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.test.common.TestResourceScope;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.yanmastra.quarkusBase.cache.FileKeyValueCacheStore;
import io.yanmastra.quarkusBase.cache.KeyValueCacheStore;
import io.yanmastra.quarkusBase.utils.KeyValueCacheUtils;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static io.yanmastra.quarkus.rediscache.LegacyFileCacheResource.LEGACY_COOKIES;
import static io.yanmastra.quarkus.rediscache.LegacyFileCacheResource.LEGACY_SESSIONS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The application starts with "old" cache files already on disk (see {@link LegacyFileCacheResource}); the extension
 * must have moved them into Redis by the time the first test runs. The second half drives the way back.
 */
@QuarkusTest
@WithTestResource(value = LegacyFileCacheResource.class, scope = TestResourceScope.GLOBAL)
class MigrationTest {

    @Inject
    RedisKeyValueCacheStore store;
    @Inject
    RedisDataSource redis;
    @Inject
    RedisCacheConfig config;
    @ConfigProperty(name = "cache_directory")
    String cacheDirectory;

    /** Whatever a test did, leave Redis as the active store for the tests that run after it. */
    @AfterEach
    void useRedisAgain() {
        KeyValueCacheUtils.registerStore(store);
    }

    private File cacheDir() {
        return new File(cacheDirectory, ".cache_v2");
    }

    private FileKeyValueCacheStore files() {
        return new FileKeyValueCacheStore(cacheDirectory);
    }

    // --- files -> Redis, at application start ---

    @Test
    void oldFileCacheWasMovedToRedisAtStartup() {
        assertEquals("user-1", KeyValueCacheUtils.findCache(LEGACY_SESSIONS, "s1"));
        assertEquals("user-2", KeyValueCacheUtils.findCache(LEGACY_SESSIONS, "s2"));
        assertEquals("cookie-token-1", KeyValueCacheUtils.findCache(LEGACY_COOKIES, "c1"));

        assertEquals("user-1", store.get(LEGACY_SESSIONS, "s1"), "the data must really be in Redis");
    }

    @Test
    void oldFilesWereKeptAsBackupsAndAreNotMigratedAgain() {
        assertTrue(new File(cacheDir(), ".cache." + LEGACY_SESSIONS + ".migrated").exists());
        assertFalse(new File(cacheDir(), ".cache." + LEGACY_SESSIONS).exists());
        assertTrue(files().listCacheNames().isEmpty());
    }

    @Test
    void migratingNeverOverwritesNewerRedisData() {
        String cache = "merge-" + UUID.randomUUID();
        KeyValueCacheUtils.saveCache(cache, "k", "newer-in-redis");

        // an old file with the same key appears, e.g. from an instance that was still running without Redis
        files().put(cache, "k", "stale-in-file");
        files().put(cache, "only-in-file", "kept");
        KeyValueCacheUtils.registerStore(store);

        assertEquals("newer-in-redis", KeyValueCacheUtils.findCache(cache, "k"));
        assertEquals("kept", KeyValueCacheUtils.findCache(cache, "only-in-file"));
    }

    // --- Redis -> files (cache.redis.enabled=false) ---

    @Test
    void disabledStoreMovesEverythingBackIntoFilesAndEmptiesRedis() {
        String cache = "back-" + UUID.randomUUID();
        KeyValueCacheUtils.saveCache(cache, "s1", "user-1");
        KeyValueCacheUtils.saveCache(cache, "s2", "user-2");

        KeyValueCacheUtils.registerStore(new Disabled(store));

        assertEquals("user-1", files().get(cache, "s1"), "data must now be in local files");
        assertEquals("user-2", files().get(cache, "s2"));
        assertEquals("user-1", KeyValueCacheUtils.findCache(cache, "s1"), "and be served from files");
        assertFalse(store.listCacheNames().contains(cache), "Redis must be drained");
        assertTrue(store.listCacheNames().isEmpty(), "every cache must have left Redis: " + store.listCacheNames());
    }

    @Test
    void whileDisabledNewWritesGoToFilesNotToRedis() {
        String cache = "disabled-" + UUID.randomUUID();
        KeyValueCacheUtils.registerStore(new Disabled(store));

        KeyValueCacheUtils.saveCache(cache, "k", "v");

        assertEquals("v", files().get(cache, "k"));
        assertNull(store.get(cache, "k"));
    }

    @Test
    void movingBackAndForthLosesNothing() {
        String cache = "roundtrip-" + UUID.randomUUID();
        Map<String, String> data = Map.of("a", "1", "b", "2", "c", "3");
        data.forEach((k, v) -> KeyValueCacheUtils.saveCache(cache, k, v));

        KeyValueCacheUtils.registerStore(new Disabled(store));   // Redis -> files
        KeyValueCacheUtils.registerStore(store);                 // files -> Redis

        assertEquals(data, store.entries(cache));
        assertEquals("1", KeyValueCacheUtils.findCache(cache, "a"));
    }

    @Test
    void anInstanceHoldingTheMigrationLockMakesOthersSkip() {
        String cache = "locked-" + UUID.randomUUID();
        files().put(cache, "k", "v");

        assertTrue(store.tryLock("migration", Duration.ofSeconds(30)), "another instance takes the lock");
        try {
            KeyValueCacheUtils.registerStore(store);
            assertNull(store.get(cache, "k"), "must not migrate while another instance is migrating");
            assertTrue(files().listCacheNames().contains(cache), "the files must stay untouched");
        } finally {
            store.unlock("migration");
        }

        KeyValueCacheUtils.registerStore(store);
        assertEquals("v", store.get(cache, "k"), "and pick the data up on the next start");
    }

    /** The same store, but reporting cache.redis.enabled=false. */
    static class Disabled implements KeyValueCacheStore {
        private final KeyValueCacheStore delegate;

        Disabled(KeyValueCacheStore delegate) {
            this.delegate = delegate;
        }

        @Override public boolean isEnabled() { return false; }
        @Override public String get(String cacheName, String key) { return delegate.get(cacheName, key); }
        @Override public void put(String cacheName, String key, String value) { delegate.put(cacheName, key, value); }
        @Override public void remove(String cacheName, String key) { delegate.remove(cacheName, key); }
        @Override public Uni<String> getAsync(String cacheName, String key) { return delegate.getAsync(cacheName, key); }
        @Override public Set<String> listCacheNames() { return delegate.listCacheNames(); }
        @Override public Map<String, String> entries(String cacheName) { return delegate.entries(cacheName); }
        @Override public boolean putIfAbsent(String cacheName, String key, String value) { return delegate.putIfAbsent(cacheName, key, value); }
        @Override public void markMigrated(String cacheName) { delegate.markMigrated(cacheName); }
        @Override public boolean tryLock(String lockName, Duration ttl) { return delegate.tryLock(lockName, ttl); }
        @Override public void unlock(String lockName) { delegate.unlock(lockName); }
    }
}
