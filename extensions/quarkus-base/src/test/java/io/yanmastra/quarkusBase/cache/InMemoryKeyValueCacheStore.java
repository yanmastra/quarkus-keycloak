package io.yanmastra.quarkusBase.cache;

import java.time.Duration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Stand-in for a remote store (e.g. Redis) so migration logic can be tested without one. */
public class InMemoryKeyValueCacheStore implements KeyValueCacheStore {
    public final Map<String, Map<String, String>> data = new ConcurrentHashMap<>();
    public final Set<String> migratedCaches = new HashSet<>();
    public boolean enabled = true;
    public boolean lockAvailable = true;
    public int lockCalls = 0;
    public int unlockCalls = 0;
    /** When set, putAllIfAbsent fails for this cache name. */
    public String failOnCache = null;

    @Override
    public String get(String cacheName, String key) {
        Map<String, String> cache = data.get(cacheName);
        return cache == null ? null : cache.get(key);
    }

    @Override
    public void put(String cacheName, String key, String value) {
        data.computeIfAbsent(cacheName, k -> new ConcurrentHashMap<>()).put(key, value);
    }

    @Override
    public void remove(String cacheName, String key) {
        Map<String, String> cache = data.get(cacheName);
        if (cache != null) cache.remove(key);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public Set<String> listCacheNames() {
        Set<String> names = new HashSet<>();
        data.forEach((name, cache) -> {
            if (!cache.isEmpty()) names.add(name);
        });
        return names;
    }

    @Override
    public Map<String, String> entries(String cacheName) {
        return new LinkedHashMap<>(data.getOrDefault(cacheName, Map.of()));
    }

    @Override
    public boolean putIfAbsent(String cacheName, String key, String value) {
        if (cacheName.equals(failOnCache)) throw new IllegalStateException("simulated failure on " + cacheName);
        return data.computeIfAbsent(cacheName, k -> new ConcurrentHashMap<>()).putIfAbsent(key, value) == null;
    }

    @Override
    public void markMigrated(String cacheName) {
        data.remove(cacheName);
        migratedCaches.add(cacheName);
    }

    @Override
    public boolean tryLock(String lockName, Duration ttl) {
        lockCalls++;
        return lockAvailable;
    }

    @Override
    public void unlock(String lockName) {
        unlockCalls++;
    }
}
