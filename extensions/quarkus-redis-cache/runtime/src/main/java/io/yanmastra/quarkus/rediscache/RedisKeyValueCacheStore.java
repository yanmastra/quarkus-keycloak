package io.yanmastra.quarkus.rediscache;

import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.hash.HashCommands;
import io.quarkus.redis.datasource.hash.ReactiveHashCommands;
import io.quarkus.redis.datasource.keys.KeyScanArgs;
import io.quarkus.redis.datasource.keys.KeyScanCursor;
import io.quarkus.redis.datasource.value.ValueCommands;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Context;
import io.yanmastra.quarkusBase.cache.KeyValueCacheStore;
import io.yanmastra.quarkusBase.utils.KeyValueCacheUtils;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.interceptor.Interceptor;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link KeyValueCacheStore} backed by Redis. Every cache is one hash ({@code <prefix>:<cacheName>}) whose fields are
 * the cache keys, so a read or a write touches a single field instead of rewriting a whole file.
 * <p>
 * Registers itself with {@link KeyValueCacheUtils} at start-up, which also migrates any data still in local files.
 */
@ApplicationScoped
public class RedisKeyValueCacheStore implements KeyValueCacheStore {
    private static final Logger logger = Logger.getLogger(RedisKeyValueCacheStore.class.getName());

    private static final String META = "meta:";
    private static final String UNLOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    @Inject
    RedisDataSource redis;
    @Inject
    ReactiveRedisDataSource reactiveRedis;
    @Inject
    RedisCacheConfig config;

    private HashCommands<String, String, String> hash;
    private ReactiveHashCommands<String, String, String> reactiveHash;
    private volatile CacheCipher cipher;
    private final Map<String, String> heldLocks = new ConcurrentHashMap<>();

    // --- life cycle ---

    void onStart(@Observes @Priority(Interceptor.Priority.LIBRARY_BEFORE) StartupEvent event) {
        try {
            if (!initialize()) return;
            KeyValueCacheUtils.registerStore(this);
        } catch (RuntimeException e) {
            if (config.enabled()) {
                throw new IllegalStateException("Could not start the Redis key/value cache: " + e.getMessage()
                        + ". Check quarkus.redis.hosts, or set cache.redis.enabled=false to use local files.", e);
            }
            logger.warn("cache.redis.enabled=false but Redis could not be reached, so its data was NOT moved back to "
                    + "local files: " + e.getMessage());
        }
    }

    void onStop(@Observes ShutdownEvent event) {
        KeyValueCacheUtils.unregisterStore(this);
    }

    /**
     * @return {@code false} when there is nothing to do: the store is disabled and Redis holds no encryption key,
     * so no cache data can exist there.
     */
    boolean initialize() {
        hash = redis.hash(String.class);
        reactiveHash = reactiveRedis.hash(String.class);

        if (!config.encrypt()) return true;

        ValueCommands<String, String> values = redis.value(String.class);
        String keyName = metaKey("key");
        String base64Key = values.get(keyName);
        if (base64Key == null) {
            if (!config.enabled()) return false;
            // several instances may start at once: the first SETNX wins and everybody reads the winner's key
            values.setnx(keyName, CacheCipher.generateBase64Key());
            base64Key = values.get(keyName);
        }
        cipher = new CacheCipher(base64Key);
        return true;
    }

    // --- KeyValueCacheStore: blocking ---

    @Override
    public String get(String cacheName, String key) {
        assertWorkerThread();
        return decode(hash.hget(hashKey(cacheName), key));
    }

    @Override
    public void put(String cacheName, String key, String value) {
        assertWorkerThread();
        hash.hset(hashKey(cacheName), key, encode(value));
    }

    @Override
    public void remove(String cacheName, String key) {
        assertWorkerThread();
        hash.hdel(hashKey(cacheName), key);
    }

    // --- KeyValueCacheStore: non-blocking ---

    @Override
    public Uni<String> getAsync(String cacheName, String key) {
        return reactiveHash.hget(hashKey(cacheName), key).map(this::decode);
    }

    @Override
    public Uni<Void> putAsync(String cacheName, String key, String value) {
        return reactiveHash.hset(hashKey(cacheName), key, encode(value)).replaceWithVoid();
    }

    @Override
    public Uni<Void> removeAsync(String cacheName, String key) {
        return reactiveHash.hdel(hashKey(cacheName), key).replaceWithVoid();
    }

    // --- KeyValueCacheStore: migration ---

    @Override
    public boolean isEnabled() {
        return config.enabled();
    }

    @Override
    public Set<String> listCacheNames() {
        assertWorkerThread();
        String prefix = config.keyPrefix() + ":";
        Set<String> names = new HashSet<>();

        KeyScanCursor<String> cursor = redis.key().scan(new KeyScanArgs().match(prefix + "*").count(100));
        while (cursor.hasNext()) {
            for (String redisKey : cursor.next()) {
                String name = redisKey.substring(prefix.length());
                if (!name.startsWith(META)) names.add(name);
            }
        }
        return names;
    }

    @Override
    public Map<String, String> entries(String cacheName) {
        assertWorkerThread();
        Map<String, String> entries = new LinkedHashMap<>();
        for (Map.Entry<String, String> stored : hash.hgetall(hashKey(cacheName)).entrySet()) {
            String value = decode(stored.getValue());
            if (value != null) entries.put(stored.getKey(), value);
        }
        return entries;
    }

    @Override
    public boolean putIfAbsent(String cacheName, String key, String value) {
        assertWorkerThread();
        return hash.hsetnx(hashKey(cacheName), key, encode(value));
    }

    @Override
    public void markMigrated(String cacheName) {
        assertWorkerThread();
        redis.key().del(hashKey(cacheName));
    }

    @Override
    public boolean tryLock(String lockName, Duration ttl) {
        assertWorkerThread();
        String token = UUID.randomUUID().toString();
        // SET ... NX PX is atomic and the TTL means a crashed instance cannot block the migration forever
        Object reply = redis.execute("SET", metaKey("lock:" + lockName), token, "NX", "PX", String.valueOf(ttl.toMillis()));
        if (reply == null) return false;
        heldLocks.put(lockName, token);
        return true;
    }

    @Override
    public void unlock(String lockName) {
        String token = heldLocks.remove(lockName);
        if (token == null) return;
        redis.execute("EVAL", UNLOCK_SCRIPT, "1", metaKey("lock:" + lockName), token);
    }

    // --- helpers ---

    private String hashKey(String cacheName) {
        return config.keyPrefix() + ":" + cacheName;
    }

    private String metaKey(String name) {
        return config.keyPrefix() + ":" + META + name;
    }

    private String encode(String value) {
        CacheCipher current = cipher;
        return current == null ? value : current.encrypt(value);
    }

    private String decode(String stored) {
        if (stored == null) return null;
        CacheCipher current = cipher;
        return current == null ? stored : current.decrypt(stored);
    }

    /**
     * The blocking Redis client cannot be used on an event-loop thread; fail with a message that says what to call
     * instead of Quarkus' generic "cannot be blocked" error.
     */
    private static void assertWorkerThread() {
        if (Context.isOnEventLoopThread()) {
            throw new IllegalStateException("The Redis key/value cache was called with a blocking method on an "
                    + "event-loop thread. Use KeyValueCacheUtils.findCacheAsync/saveCacheAsync/removeCacheAsync "
                    + "(or the *Async methods of AuthenticationService) from reactive endpoints.");
        }
    }
}
