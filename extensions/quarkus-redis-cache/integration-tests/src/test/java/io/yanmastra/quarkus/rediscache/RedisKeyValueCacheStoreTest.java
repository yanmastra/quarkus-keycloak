package io.yanmastra.quarkus.rediscache;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.Vertx;
import io.yanmastra.quarkusBase.utils.KeyValueCacheUtils;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class RedisKeyValueCacheStoreTest {

    @Inject
    RedisKeyValueCacheStore store;
    @Inject
    RedisDataSource redis;
    @Inject
    RedisCacheConfig config;
    @Inject
    Vertx vertx;

    // every test uses its own cache name, so tests never see each other's data
    private final String cache = "test-" + UUID.randomUUID();

    private String hashKey() {
        return config.keyPrefix() + ":" + cache;
    }

    private String rawValue(String key) {
        return redis.hash(String.class).hget(hashKey(), key);
    }

    // --- basic behaviour ---

    @Test
    void putThenGet() {
        store.put(cache, "k", "v");
        assertEquals("v", store.get(cache, "k"));
    }

    @Test
    void missingKeyOrCacheReturnsNull() {
        assertNull(store.get(cache, "nothing"));
        store.put(cache, "k", "v");
        assertNull(store.get(cache, "other"));
    }

    @Test
    void putOverwrites() {
        store.put(cache, "k", "old");
        store.put(cache, "k", "new");
        assertEquals("new", store.get(cache, "k"));
        assertEquals(1, store.entries(cache).size());
    }

    @Test
    void removeDeletesOnlyThatKey() {
        store.put(cache, "a", "1");
        store.put(cache, "b", "2");
        store.remove(cache, "a");
        assertNull(store.get(cache, "a"));
        assertEquals("2", store.get(cache, "b"));
    }

    @Test
    void valuesWithSpecialCharactersRoundTrip() {
        String value = "line1\nline2 \"quoted\" {json: [1,2]} ünïcödé";
        store.put(cache, "k", value);
        assertEquals(value, store.get(cache, "k"));
    }

    @Test
    void listCacheNamesFindsCachesButNotReservedKeys() {
        store.put(cache, "k", "v");
        Set<String> names = store.listCacheNames();
        assertTrue(names.contains(cache));
        assertTrue(names.stream().noneMatch(n -> n.startsWith("meta:")), "meta keys must be hidden: " + names);
    }

    @Test
    void putIfAbsentKeepsTheExistingValue() {
        store.put(cache, "k", "existing");
        assertFalse(store.putIfAbsent(cache, "k", "incoming"));
        assertTrue(store.putIfAbsent(cache, "k2", "new"));
        assertEquals("existing", store.get(cache, "k"));
        assertEquals("new", store.get(cache, "k2"));
    }

    @Test
    void markMigratedDeletesTheCache() {
        store.put(cache, "k", "v");
        store.markMigrated(cache);
        assertFalse(store.listCacheNames().contains(cache));
        assertNull(store.get(cache, "k"));
    }

    // --- non-blocking API ---

    @Test
    void asyncMethodsWork() {
        store.putAsync(cache, "k", "v").await().indefinitely();
        assertEquals("v", store.getAsync(cache, "k").await().indefinitely());
        assertEquals("v", store.get(cache, "k"), "blocking and async views must agree");

        store.removeAsync(cache, "k").await().indefinitely();
        assertNull(store.getAsync(cache, "k").await().indefinitely());
    }

    @Test
    void asyncMethodsWorkOnTheEventLoop() throws Exception {
        CompletableFuture<String> result = new CompletableFuture<>();
        vertx.getOrCreateContext().runOnContext(v ->
                KeyValueCacheUtils.saveCacheAsync(cache, "k", "from-event-loop")
                        .chain(() -> KeyValueCacheUtils.findCacheAsync(cache, "k"))
                        .subscribe().with(result::complete, result::completeExceptionally));

        assertEquals("from-event-loop", result.get(10, TimeUnit.SECONDS));
    }

    @Test
    void blockingMethodsOnTheEventLoopFailWithAHelpfulMessage() throws Exception {
        CompletableFuture<Throwable> failure = new CompletableFuture<>();
        vertx.getOrCreateContext().runOnContext(v -> {
            try {
                store.get(cache, "k");
                failure.complete(null);
            } catch (Throwable t) {
                failure.complete(t);
            }
        });

        Throwable thrown = failure.get(10, TimeUnit.SECONDS);
        assertInstanceOf(IllegalStateException.class, thrown);
        assertTrue(thrown.getMessage().contains("findCacheAsync"), thrown.getMessage());
    }

    // --- used through KeyValueCacheUtils, which is what the other extensions call ---

    @Test
    void keyValueCacheUtilsIsBackedByRedis() {
        KeyValueCacheUtils.saveCache(cache, "k", "v");

        assertEquals("v", KeyValueCacheUtils.findCache(cache, "k"));
        assertNotNull(rawValue("k"), "the value must be in the Redis hash " + hashKey());

        KeyValueCacheUtils.saveCache(cache, "k", "");
        assertNull(rawValue("k"), "saving a blank value removes the field");
    }

    // --- encryption ---

    @Test
    void valuesAreEncryptedInRedis() {
        store.put(cache, "k", "very-secret-value");
        String raw = rawValue("k");

        assertNotNull(raw);
        assertFalse(raw.contains("very-secret-value"));
    }

    @Test
    void theKeyIsSharedThroughRedisSoAnotherInstanceCanReadTheData() {
        store.put(cache, "k", "shared-secret");
        String base64Key = redis.value(String.class).get(config.keyPrefix() + ":meta:key");
        assertNotNull(base64Key, "the encryption key must be stored in Redis");

        // what a second application instance does: read the key from Redis and use it
        CacheCipher otherInstance = new CacheCipher(base64Key);
        assertEquals("shared-secret", otherInstance.decrypt(rawValue("k")));
    }

    @Test
    void aWrongKeyReadsAsMissingInsteadOfFailing() {
        store.put(cache, "k", "v");
        CacheCipher stranger = new CacheCipher(CacheCipher.generateBase64Key());
        assertNull(stranger.decrypt(rawValue("k")));
    }

    // --- migration lock ---

    @Test
    void lockIsExclusiveUntilReleased() {
        String lock = "test-lock-" + UUID.randomUUID();

        assertTrue(store.tryLock(lock, Duration.ofSeconds(30)));
        assertFalse(store.tryLock(lock, Duration.ofSeconds(30)), "a held lock must not be granted again");

        store.unlock(lock);
        assertTrue(store.tryLock(lock, Duration.ofSeconds(30)), "the lock must be free after unlock");
        store.unlock(lock);
    }

    @Test
    void lockExpiresOnItsOwnSoACrashedInstanceCannotBlockMigrationForever() throws Exception {
        String lock = "test-lock-" + UUID.randomUUID();

        assertTrue(store.tryLock(lock, Duration.ofMillis(200)));
        Thread.sleep(400);
        assertTrue(store.tryLock(lock, Duration.ofSeconds(30)));
        store.unlock(lock);
    }

    @Test
    void entriesReturnsEverythingInTheCache() {
        store.put(cache, "a", "1");
        store.put(cache, "b", "2");
        assertEquals(Map.of("a", "1", "b", "2"), store.entries(cache));
    }
}
