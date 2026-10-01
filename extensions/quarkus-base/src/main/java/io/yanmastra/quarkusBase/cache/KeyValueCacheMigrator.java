package io.yanmastra.quarkusBase.cache;

import org.jboss.logging.Logger;

import java.time.Duration;
import java.util.Map;

/**
 * Moves every cache from one {@link KeyValueCacheStore} to another. Values already present in the target are
 * never overwritten, so it is safe to run repeatedly and from several instances.
 * <p>
 * A cache is handed to {@link KeyValueCacheStore#markMigrated(String)} on the source only after all of its
 * entries were written to the target, so an interrupted migration loses nothing and simply resumes next start.
 */
public final class KeyValueCacheMigrator {
    private static final Logger logger = Logger.getLogger(KeyValueCacheMigrator.class.getName());

    static final String LOCK_NAME = "migration";
    private static final Duration LOCK_TTL = Duration.ofMinutes(2);

    public record Result(int caches, int entries) {
        static final Result NOTHING = new Result(0, 0);
    }

    private KeyValueCacheMigrator() {
    }

    /**
     * @param sourceName / targetName only used for logging
     * @param lockStore the store whose lock guards the migration (the one shared between instances)
     */
    public static Result migrate(KeyValueCacheStore source, String sourceName,
                                 KeyValueCacheStore target, String targetName,
                                 KeyValueCacheStore lockStore) {
        if (source.listCacheNames().isEmpty()) return Result.NOTHING;

        if (!lockStore.tryLock(LOCK_NAME, LOCK_TTL)) {
            logger.infof("Cache migration %s -> %s is already running on another instance, skipping", sourceName, targetName);
            return Result.NOTHING;
        }
        try {
            int caches = 0, entries = 0;
            for (String cacheName : source.listCacheNames()) {
                Map<String, String> data = source.entries(cacheName);
                entries += target.putAllIfAbsent(cacheName, data);
                source.markMigrated(cacheName);
                caches++;
                logger.debugf("Migrated cache '%s' (%d entries) from %s to %s", cacheName, data.size(), sourceName, targetName);
            }
            logger.infof("Cache migration %s -> %s finished: %d cache(s), %d entries written", sourceName, targetName, caches, entries);
            return new Result(caches, entries);
        } catch (RuntimeException e) {
            throw new IllegalStateException("Cache migration " + sourceName + " -> " + targetName + " failed: " + e.getMessage(), e);
        } finally {
            lockStore.unlock(LOCK_NAME);
        }
    }
}
