package io.yanmastra.quarkusBase.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FileKeyValueCacheStoreTest {

    @TempDir
    Path dir;

    FileKeyValueCacheStore store;

    @BeforeEach
    void setUp() {
        store = new FileKeyValueCacheStore(dir.toString());
    }

    private File cacheDir() {
        return new File(dir.toFile(), FileKeyValueCacheStore.CACHE_DIR);
    }

    @Test
    void putThenGet() {
        store.put("sessions", "s1", "user-1");
        assertEquals("user-1", store.get("sessions", "s1"));
    }

    @Test
    void getMissingKeyOrCacheReturnsNull() {
        assertNull(store.get("nothing", "k"));
        store.put("sessions", "s1", "v");
        assertNull(store.get("sessions", "other"));
    }

    @Test
    void putOverwritesExistingValue() {
        store.put("sessions", "s1", "old");
        store.put("sessions", "s1", "new");
        assertEquals("new", store.get("sessions", "s1"));
        assertEquals(1, store.entries("sessions").size());
    }

    @Test
    void cachesAreIndependent() {
        store.put("a", "k", "from-a");
        store.put("b", "k", "from-b");
        assertEquals("from-a", store.get("a", "k"));
        assertEquals("from-b", store.get("b", "k"));
    }

    @Test
    void removeDeletesOnlyThatKey() {
        store.put("sessions", "s1", "v1");
        store.put("sessions", "s2", "v2");
        store.remove("sessions", "s1");
        assertNull(store.get("sessions", "s1"));
        assertEquals("v2", store.get("sessions", "s2"));
    }

    @Test
    void removingLastKeyDeletesTheFile() {
        store.put("sessions", "s1", "v1");
        store.remove("sessions", "s1");
        assertFalse(new File(cacheDir(), ".cache.sessions").exists());
        assertTrue(store.listCacheNames().isEmpty());
    }

    @Test
    void removeMissingKeyIsNotAnError() {
        assertDoesNotThrow(() -> store.remove("sessions", "never-existed"));
    }

    @Test
    void dataSurvivesANewStoreInstanceOnTheSameDirectory() {
        store.put("sessions", "s1", "v1");
        FileKeyValueCacheStore reopened = new FileKeyValueCacheStore(dir.toString());
        assertEquals("v1", reopened.get("sessions", "s1"));
    }

    @Test
    void valuesAreEncryptedOnDisk() throws Exception {
        store.put("sessions", "s1", "very-secret-value");
        byte[] raw = Files.readAllBytes(new File(cacheDir(), ".cache.sessions").toPath());
        assertFalse(new String(raw).contains("very-secret-value"));
        assertFalse(new String(raw).contains("s1"));
    }

    @Test
    void valuesWithSpecialCharactersRoundTrip() {
        String value = "line1\nline2 \"quoted\" {json: [1,2]} ünïcödé";
        store.put("sessions", "k", value);
        assertEquals(value, store.get("sessions", "k"));
    }

    @Test
    void listCacheNamesIgnoresKeyFileAndBackups() throws Exception {
        store.put("sessions", "s1", "v");
        store.put("cookies", "c1", "v");
        Files.write(new File(cacheDir(), ".cache.old.migrated").toPath(), new byte[]{1, 2, 3});

        assertEquals(Set.of("sessions", "cookies"), store.listCacheNames());
    }

    @Test
    void putIfAbsentKeepsExistingValue() {
        store.put("sessions", "s1", "existing");
        assertFalse(store.putIfAbsent("sessions", "s1", "incoming"));
        assertTrue(store.putIfAbsent("sessions", "s2", "new"));
        assertEquals("existing", store.get("sessions", "s1"));
        assertEquals("new", store.get("sessions", "s2"));
    }

    @Test
    void putAllIfAbsentReturnsNumberOfWrittenEntries() {
        store.put("sessions", "s1", "existing");
        int written = store.putAllIfAbsent("sessions", Map.of("s1", "x", "s2", "y", "s3", "z"));
        assertEquals(2, written);
        assertEquals("existing", store.get("sessions", "s1"));
    }

    @Test
    void markMigratedKeepsABackupAndHidesTheCache() {
        store.put("sessions", "s1", "v1");
        store.markMigrated("sessions");

        assertFalse(new File(cacheDir(), ".cache.sessions").exists());
        assertTrue(new File(cacheDir(), ".cache.sessions.migrated").exists());
        assertTrue(store.listCacheNames().isEmpty());
        assertNull(store.get("sessions", "s1"));
    }

    @Test
    void corruptedFileIsTreatedAsEmptyAndCanBeWrittenAgain() throws Exception {
        store.put("sessions", "s1", "v1");
        Files.write(new File(cacheDir(), ".cache.sessions").toPath(), new byte[64]);

        assertNull(store.get("sessions", "s1"));
        store.put("sessions", "s2", "v2");
        assertEquals("v2", store.get("sessions", "s2"));
    }
}
