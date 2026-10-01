package io.yanmastra.quarkusBase.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class KeyValueCacheMigratorTest {

    @TempDir
    Path dir;

    FileKeyValueCacheStore files;
    InMemoryKeyValueCacheStore remote;

    @BeforeEach
    void setUp() {
        files = new FileKeyValueCacheStore(dir.toString());
        remote = new InMemoryKeyValueCacheStore();
    }

    private KeyValueCacheMigrator.Result filesToRemote() {
        return KeyValueCacheMigrator.migrate(files, "files", remote, "remote", remote);
    }

    private KeyValueCacheMigrator.Result remoteToFiles() {
        return KeyValueCacheMigrator.migrate(remote, "remote", files, "files", remote);
    }

    // --- files -> remote ---

    @Test
    void movesEveryCacheAndEntryFromFilesToRemote() {
        files.put("sessions", "s1", "u1");
        files.put("sessions", "s2", "u2");
        files.put("cookies", "c1", "token");

        KeyValueCacheMigrator.Result result = filesToRemote();

        assertEquals(2, result.caches());
        assertEquals(3, result.entries());
        assertEquals("u1", remote.get("sessions", "s1"));
        assertEquals("u2", remote.get("sessions", "s2"));
        assertEquals("token", remote.get("cookies", "c1"));
    }

    @Test
    void sourceFilesAreArchivedAfterMigration() {
        files.put("sessions", "s1", "u1");

        filesToRemote();

        assertTrue(files.listCacheNames().isEmpty(), "migrated files must not be detected again");
        assertNull(files.get("sessions", "s1"));
    }

    @Test
    void runningTwiceDoesNothingTheSecondTime() {
        files.put("sessions", "s1", "u1");

        filesToRemote();
        KeyValueCacheMigrator.Result second = filesToRemote();

        assertEquals(0, second.caches());
        assertEquals(0, second.entries());
        assertEquals("u1", remote.get("sessions", "s1"));
    }

    @Test
    void neverOverwritesNewerValuesInTheTarget() {
        remote.put("sessions", "s1", "newer");
        files.put("sessions", "s1", "stale");
        files.put("sessions", "s2", "only-in-file");

        KeyValueCacheMigrator.Result result = filesToRemote();

        assertEquals("newer", remote.get("sessions", "s1"));
        assertEquals("only-in-file", remote.get("sessions", "s2"));
        assertEquals(1, result.entries());
    }

    @Test
    void mergesIntoAnExistingTargetCache() {
        remote.put("sessions", "existing", "e");
        files.put("sessions", "fromFile", "f");

        filesToRemote();

        assertEquals(Map.of("existing", "e", "fromFile", "f"), remote.entries("sessions"));
    }

    @Test
    void nothingToMigrateDoesNotTouchTheLock() {
        KeyValueCacheMigrator.Result result = filesToRemote();

        assertEquals(0, result.caches());
        assertEquals(0, remote.lockCalls);
    }

    // --- remote -> files (migrate back) ---

    @Test
    void movesEverythingBackFromRemoteToFiles() {
        remote.put("sessions", "s1", "u1");
        remote.put("cookies", "c1", "token");

        KeyValueCacheMigrator.Result result = remoteToFiles();

        assertEquals(2, result.caches());
        assertEquals("u1", files.get("sessions", "s1"));
        assertEquals("token", files.get("cookies", "c1"));
        assertTrue(remote.listCacheNames().isEmpty(), "remote must be drained");
        assertEquals(Set.of("sessions", "cookies"), remote.migratedCaches);
    }

    @Test
    void roundTripPreservesData() {
        files.put("sessions", "s1", "u1");
        files.put("sessions", "s2", "u2");

        filesToRemote();
        remoteToFiles();

        assertEquals(Map.of("s1", "u1", "s2", "u2"), files.entries("sessions"));
        assertTrue(remote.listCacheNames().isEmpty());
    }

    // --- locking & failure ---

    @Test
    void skipsWhenAnotherInstanceHoldsTheLock() {
        files.put("sessions", "s1", "u1");
        remote.lockAvailable = false;

        KeyValueCacheMigrator.Result result = filesToRemote();

        assertEquals(0, result.caches());
        assertNull(remote.get("sessions", "s1"));
        assertEquals(Set.of("sessions"), files.listCacheNames(), "source must stay untouched");
        assertEquals(0, remote.unlockCalls, "must not unlock a lock it does not own");
    }

    @Test
    void releasesTheLockAfterSuccess() {
        files.put("sessions", "s1", "u1");
        filesToRemote();
        assertEquals(1, remote.lockCalls);
        assertEquals(1, remote.unlockCalls);
    }

    @Test
    void failureKeepsTheFailedCacheInTheSourceAndReleasesTheLock() {
        files.put("good", "k", "v");
        files.put("bad", "k", "v");
        remote.failOnCache = "bad";

        IllegalStateException e = assertThrows(IllegalStateException.class, this::filesToRemote);

        assertTrue(e.getMessage().contains("failed"));
        assertTrue(files.listCacheNames().contains("bad"), "the cache that failed must still be in the source");
        assertEquals(1, remote.unlockCalls);

        // and it resumes on the next start
        remote.failOnCache = null;
        filesToRemote();
        assertEquals("v", remote.get("bad", "k"));
        assertTrue(files.listCacheNames().isEmpty());
    }
}
