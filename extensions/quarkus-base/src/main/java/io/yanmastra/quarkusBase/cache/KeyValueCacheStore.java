package io.yanmastra.quarkusBase.cache;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.vertx.core.Context;
import io.vertx.core.Vertx;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Storage backend behind {@link io.yanmastra.quarkusBase.utils.KeyValueCacheUtils}.
 * <p>
 * The default backend keeps the cache in encrypted local files. Another extension (for example
 * {@code quarkus-redis-cache}) can provide a different backend by calling
 * {@code KeyValueCacheUtils.registerStore(...)}; when one is registered, all cache reads and writes go to it
 * and any data left in the previous backend is migrated automatically.
 * <p>
 * The blocking methods must only be called from a worker thread. The {@code *Async} methods are safe to call
 * from the event loop; by default they run the blocking method on Mutiny's worker pool, and a backend with a
 * natively non-blocking client should override them.
 */
public interface KeyValueCacheStore {

    // --- blocking access ---

    /** @return the stored value, or {@code null} when the key is not present. */
    String get(String cacheName, String key);

    /** Stores {@code value} under {@code key}, replacing any previous value. {@code value} is never blank. */
    void put(String cacheName, String key, String value);

    /** Removes {@code key}. Removing a missing key is not an error. */
    void remove(String cacheName, String key);

    // --- non-blocking access ---

    default Uni<String> getAsync(String cacheName, String key) {
        return onWorkerPool(() -> get(cacheName, key));
    }

    default Uni<Void> putAsync(String cacheName, String key, String value) {
        return onWorkerPool(() -> {
            put(cacheName, key, value);
            return null;
        });
    }

    default Uni<Void> removeAsync(String cacheName, String key) {
        return onWorkerPool(() -> {
            remove(cacheName, key);
            return null;
        });
    }

    // --- migration support ---

    /**
     * A store that is not enabled is never used for reads/writes. When it is registered anyway, its content is
     * drained into the local file store. This is how data is moved back from e.g. Redis to files: keep the
     * extension on the classpath, disable it, start once, then remove it.
     */
    default boolean isEnabled() {
        return true;
    }

    /** @return the names of all caches that currently hold data in this store. */
    Set<String> listCacheNames();

    /** @return every key/value pair of the given cache. Never {@code null}. */
    Map<String, String> entries(String cacheName);

    /** Writes the value only if the key is not present yet, so newer data is never overwritten by a migration. */
    boolean putIfAbsent(String cacheName, String key, String value);

    /**
     * Bulk variant of {@link #putIfAbsent}, used by migrations. A backend that rewrites storage on every write
     * (the file store) should override it to write once.
     *
     * @return the number of entries that were actually written.
     */
    default int putAllIfAbsent(String cacheName, Map<String, String> entries) {
        int written = 0;
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            if (putIfAbsent(cacheName, entry.getKey(), entry.getValue())) written++;
        }
        return written;
    }

    /**
     * Called once all entries of {@code cacheName} have been copied into another store. The file store renames
     * its file to {@code *.migrated} (kept as a backup); other stores delete the cache.
     */
    void markMigrated(String cacheName);

    /**
     * Cross-instance guard so that only one instance migrates the same store at a time.
     *
     * @return {@code true} when the lock was acquired (or the store has no need for one).
     */
    default boolean tryLock(String lockName, Duration ttl) {
        return true;
    }

    default void unlock(String lockName) {
    }

    /**
     * Runs a blocking call on the worker pool. When the caller is on a Vert.x context (e.g. the event loop while
     * authenticating a request), the result is delivered back on that same context, not on the worker thread.
     */
    static <T> Uni<T> onWorkerPool(Supplier<T> blockingCall) {
        Context caller = Vertx.currentContext();
        Uni<T> uni = Uni.createFrom().item(blockingCall).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
        if (caller == null) return uni;
        return uni.emitOn(command -> caller.runOnContext(ignored -> command.run()));
    }
}
