package io.yanmastra.quarkus.rediscache;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.yanmastra.quarkusBase.cache.FileKeyValueCacheStore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Creates "old" local-file cache data before the application starts, the way an application that ran without Redis
 * would have left it, so that the start-up migration has something to move.
 */
public class LegacyFileCacheResource implements QuarkusTestResourceLifecycleManager {
    static final String LEGACY_SESSIONS = "legacy-sessions";
    static final String LEGACY_COOKIES = "legacy-cookies";

    private Path directory;

    @Override
    public Map<String, String> start() {
        try {
            directory = Files.createTempDirectory("redis-cache-it");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        FileKeyValueCacheStore legacy = new FileKeyValueCacheStore(directory.toString());
        legacy.put(LEGACY_SESSIONS, "s1", "user-1");
        legacy.put(LEGACY_SESSIONS, "s2", "user-2");
        legacy.put(LEGACY_COOKIES, "c1", "cookie-token-1");
        return Map.of("cache_directory", directory.toString());
    }

    @Override
    public void stop() {
        if (directory == null) return;
        try (Stream<Path> files = Files.walk(directory)) {
            files.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        } catch (IOException ignored) {
            // temp directory, best effort
        }
    }
}
