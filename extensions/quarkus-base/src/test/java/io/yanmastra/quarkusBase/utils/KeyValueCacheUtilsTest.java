package io.yanmastra.quarkusBase.utils;

import io.yanmastra.quarkusBase.cache.FileKeyValueCacheStore;
import io.yanmastra.quarkusBase.cache.InMemoryKeyValueCacheStore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class KeyValueCacheUtilsTest {

    @TempDir
    static Path dir;

    static String previousCacheDirectory;

    InMemoryKeyValueCacheStore remote = new InMemoryKeyValueCacheStore();

    // Every test uses its own cache name, so tests do not see each other's data in the shared directory.
    String cache = "test-" + UUID.randomUUID();

    @BeforeAll
    static void pointCacheAtTempDirectory() {
        previousCacheDirectory = System.getProperty("cache_directory");
        System.setProperty("cache_directory", dir.toString());
    }

    @AfterAll
    static void restoreCacheDirectory() {
        if (previousCacheDirectory == null) System.clearProperty("cache_directory");
        else System.setProperty("cache_directory", previousCacheDirectory);
    }

    @AfterEach
    void backToFiles() {
        KeyValueCacheUtils.unregisterStore(remote);
    }

    private FileKeyValueCacheStore files() {
        return new FileKeyValueCacheStore(dir.toString());
    }

    // --- default (file) behaviour ---

    @Test
    void savesAndFindsWithTheDefaultFileStore() {
        KeyValueCacheUtils.saveCache(cache, "k", "v");
        assertEquals("v", KeyValueCacheUtils.findCache(cache, "k"));
        assertEquals("v", files().get(cache, "k"), "must be persisted as a local file");
    }

    @Test
    void savingABlankValueRemovesTheKey() {
        KeyValueCacheUtils.saveCache(cache, "k", "v");
        KeyValueCacheUtils.saveCache(cache, "k", "");
        assertNull(KeyValueCacheUtils.findCache(cache, "k"));
    }

    @Test
    void removeCacheRemovesTheKey() {
        KeyValueCacheUtils.saveCache(cache, "k", "v");
        KeyValueCacheUtils.removeCache(cache, "k");
        assertNull(KeyValueCacheUtils.findCache(cache, "k"));
    }

    @Test
    void blankKeyIsRejectedOnWrite() {
        assertThrows(IllegalArgumentException.class, () -> KeyValueCacheUtils.saveCache(cache, " ", "v"));
        assertThrows(IllegalArgumentException.class, () -> KeyValueCacheUtils.removeCache(cache, null));
        assertNull(KeyValueCacheUtils.findCache(cache, null));
    }

    @Test
    void asyncMethodsWorkWithTheDefaultFileStore() {
        KeyValueCacheUtils.saveCacheAsync(cache, "k", "v").await().indefinitely();
        assertEquals("v", KeyValueCacheUtils.findCacheAsync(cache, "k").await().indefinitely());

        KeyValueCacheUtils.removeCacheAsync(cache, "k").await().indefinitely();
        assertNull(KeyValueCacheUtils.findCacheAsync(cache, "k").await().indefinitely());
    }

    @Test
    void asyncBlankKeyFailsTheUniOrReturnsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> KeyValueCacheUtils.saveCacheAsync(cache, "", "v").await().indefinitely());
        assertNull(KeyValueCacheUtils.findCacheAsync(cache, "").await().indefinitely());
    }

    // --- switching to another store ---

    @Test
    void registeredStoreReceivesAllReadsAndWrites() {
        KeyValueCacheUtils.registerStore(remote);

        KeyValueCacheUtils.saveCache(cache, "k", "v");

        assertEquals("v", remote.get(cache, "k"));
        assertEquals("v", KeyValueCacheUtils.findCache(cache, "k"));
        assertNull(files().get(cache, "k"), "nothing must be written to files any more");
    }

    @Test
    void registeredStoreIsUsedByTheAsyncMethodsToo() {
        KeyValueCacheUtils.registerStore(remote);

        KeyValueCacheUtils.saveCacheAsync(cache, "k", "v").await().indefinitely();

        assertEquals("v", remote.get(cache, "k"));
        assertEquals("v", KeyValueCacheUtils.findCacheAsync(cache, "k").await().indefinitely());
    }

    @Test
    void registeringMigratesExistingFileCacheToTheNewStore() {
        KeyValueCacheUtils.saveCache(cache, "s1", "user-1");
        KeyValueCacheUtils.saveCache(cache, "s2", "user-2");

        KeyValueCacheUtils.registerStore(remote);

        assertEquals("user-1", KeyValueCacheUtils.findCache(cache, "s1"));
        assertEquals("user-2", KeyValueCacheUtils.findCache(cache, "s2"));
        assertEquals("user-1", remote.get(cache, "s1"));
        assertFalse(files().listCacheNames().contains(cache), "old file must be archived");
    }

    @Test
    void unregisteringGoesBackToFilesWithoutMigrating() {
        KeyValueCacheUtils.registerStore(remote);
        KeyValueCacheUtils.saveCache(cache, "k", "v");
        KeyValueCacheUtils.unregisterStore(remote);

        assertNull(KeyValueCacheUtils.findCache(cache, "k"));
    }

    @Test
    void aDisabledStoreIsDrainedIntoFilesAndNotUsed() {
        remote.put(cache, "s1", "user-1");
        remote.enabled = false;

        KeyValueCacheUtils.registerStore(remote);

        assertEquals("user-1", KeyValueCacheUtils.findCache(cache, "s1"));
        assertEquals("user-1", files().get(cache, "s1"), "data must now live in a local file");
        assertTrue(remote.listCacheNames().isEmpty());
        assertEquals(Set.of(cache), remote.migratedCaches);

        KeyValueCacheUtils.saveCache(cache, "s2", "user-2");
        assertNull(remote.get(cache, "s2"), "a disabled store must not receive new writes");
    }

    @Test
    void failedMigrationDoesNotRegisterTheStore() {
        KeyValueCacheUtils.saveCache(cache, "k", "v");
        remote.failOnCache = cache;

        assertThrows(IllegalStateException.class, () -> KeyValueCacheUtils.registerStore(remote));

        assertEquals("v", KeyValueCacheUtils.findCache(cache, "k"), "must keep serving from files");
        assertNull(remote.get(cache, "k"));
    }
}
