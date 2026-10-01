package io.yanmastra.quarkusBase.utils;

import io.smallrye.mutiny.Uni;
import io.yanmastra.quarkusBase.cache.FileKeyValueCacheStore;
import io.yanmastra.quarkusBase.cache.KeyValueCacheMigrator;
import io.yanmastra.quarkusBase.cache.KeyValueCacheStore;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import java.util.Objects;

/**
 * Simple key/value cache grouped by cache name.
 * <p>
 * By default the data is kept in encrypted local files ({@link FileKeyValueCacheStore}). An extension such as
 * {@code quarkus-redis-cache} can switch the storage by calling {@link #registerStore(KeyValueCacheStore)}; callers
 * of this class do not change. Data left in the previous storage is migrated automatically on registration.
 * <p>
 * The blocking methods ({@link #saveCache}, {@link #findCache}, {@link #removeCache}) must not be called from the
 * event loop when a non-file store is registered; use the {@code *Async} variants there.
 */
public class KeyValueCacheUtils {
    private static final Logger logger = Logger.getLogger(KeyValueCacheUtils.class.getName());

    // Lazy holder — nothing is created until the first cache access
    private static class FileStoreHolder {
        static final FileKeyValueCacheStore INSTANCE = new FileKeyValueCacheStore();
    }

    private static volatile KeyValueCacheStore registeredStore = null;

    // --- store selection ---

    /**
     * Makes {@code store} the storage for all caches, after moving any data still kept in local files into it.
     * If the store reports {@link KeyValueCacheStore#isEnabled()} as {@code false}, it is not used; instead its
     * content is moved back into local files (the "migrate back" path).
     *
     * @throws IllegalStateException when the migration fails, in which case the store is not registered.
     */
    public static synchronized void registerStore(KeyValueCacheStore store) {
        Objects.requireNonNull(store, "store");
        FileKeyValueCacheStore files = FileStoreHolder.INSTANCE;
        String storeName = store.getClass().getSimpleName();

        if (store.isEnabled()) {
            KeyValueCacheMigrator.migrate(files, "local files", store, storeName, store);
            registeredStore = store;
            logger.infof("Key/value cache is now stored in %s", storeName);
        } else {
            KeyValueCacheMigrator.migrate(store, storeName, files, "local files", store);
            registeredStore = null;
            logger.infof("Key/value cache is stored in local files (%s is disabled)", storeName);
        }
    }

    /** Goes back to local files without migrating; mainly for shutdown and tests. */
    public static synchronized void unregisterStore(KeyValueCacheStore store) {
        if (registeredStore == store) registeredStore = null;
    }

    private static KeyValueCacheStore store() {
        KeyValueCacheStore store = registeredStore;
        return store != null ? store : FileStoreHolder.INSTANCE;
    }

    // --- blocking API ---

    public static void removeCache(String cacheName, String key) {
        requireKey(key);
        store().remove(cacheName, key);
    }

    public static void saveCache(String cacheName, String key, String value) {
        requireKey(key);
        if (StringUtils.isBlank(value)) {
            store().remove(cacheName, key);
        } else {
            store().put(cacheName, key, value);
        }
    }

    public static String findCache(String cacheName, String key) {
        if (StringUtils.isBlank(key)) return null;
        return store().get(cacheName, key);
    }

    // --- non-blocking API ---

    public static Uni<Void> removeCacheAsync(String cacheName, String key) {
        if (StringUtils.isBlank(key)) return Uni.createFrom().failure(keyRequired());
        return store().removeAsync(cacheName, key);
    }

    public static Uni<Void> saveCacheAsync(String cacheName, String key, String value) {
        if (StringUtils.isBlank(key)) return Uni.createFrom().failure(keyRequired());
        if (StringUtils.isBlank(value)) return store().removeAsync(cacheName, key);
        return store().putAsync(cacheName, key, value);
    }

    public static Uni<String> findCacheAsync(String cacheName, String key) {
        if (StringUtils.isBlank(key)) return Uni.createFrom().nullItem();
        return store().getAsync(cacheName, key);
    }

    private static void requireKey(String key) {
        if (StringUtils.isBlank(key)) throw keyRequired();
    }

    private static IllegalArgumentException keyRequired() {
        return new IllegalArgumentException("key can't be empty");
    }
}
