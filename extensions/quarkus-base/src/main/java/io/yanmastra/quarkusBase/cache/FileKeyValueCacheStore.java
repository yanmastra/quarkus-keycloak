package io.yanmastra.quarkusBase.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import io.yanmastra.quarkusBase.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * Default {@link KeyValueCacheStore}: one AES-GCM encrypted file per cache name, kept on local disk to avoid
 * holding the data in memory. The file format is one JSON object ({@code {"key":..,"value":..}}) per line.
 */
public class FileKeyValueCacheStore implements KeyValueCacheStore {
    static final String CACHE_DIR = "/.cache_v2";
    static final String FILE_PREFIX = ".cache.";
    static final String KEY_FILE_NAME = ".cache.key";
    static final String MIGRATED_SUFFIX = ".migrated";

    private static final Logger logger = Logger.getLogger(FileKeyValueCacheStore.class.getName());

    private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;

    // Lazy holder — SecureRandom initialized at runtime, not at GraalVM build time
    private static class RandomHolder {
        static final SecureRandom INSTANCE = new SecureRandom();
    }

    // 32 fixed stripes — ~1.6 KB total memory regardless of session count.
    // Same cacheName always maps to the same stripe, so concurrent access on the same cache file is serialised.
    private static final int STRIPES = 32;
    private final ReentrantReadWriteLock[] stripeLocks = new ReentrantReadWriteLock[STRIPES];

    private final Supplier<String> baseDirectory;
    private volatile SecretKey cachedKey = null;

    /** Uses the {@code cache_directory} property, the {@code CACHE_DIRECTORY} variable, or the working directory. */
    public FileKeyValueCacheStore() {
        this(FileKeyValueCacheStore::resolveConfiguredDirectory);
    }

    public FileKeyValueCacheStore(String baseDirectory) {
        this(() -> baseDirectory);
    }

    FileKeyValueCacheStore(Supplier<String> baseDirectory) {
        this.baseDirectory = baseDirectory;
        for (int i = 0; i < STRIPES; i++) stripeLocks[i] = new ReentrantReadWriteLock();
    }

    private ReentrantReadWriteLock stripeFor(String cacheName) {
        return stripeLocks[Math.floorMod(cacheName.hashCode(), STRIPES)];
    }

    // --- KeyValueCacheStore ---

    @Override
    public String get(String cacheName, String key) {
        ReentrantReadWriteLock.ReadLock lock = stripeFor(cacheName).readLock();
        lock.lock();
        try {
            return readEntries(cacheFile(cacheName)).get(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void put(String cacheName, String key, String value) {
        ReentrantReadWriteLock.WriteLock lock = stripeFor(cacheName).writeLock();
        lock.lock();
        try {
            File file = cacheFile(cacheName);
            Map<String, String> entries = readEntries(file);
            entries.put(key, value);
            writeEntries(file, entries);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void remove(String cacheName, String key) {
        ReentrantReadWriteLock.WriteLock lock = stripeFor(cacheName).writeLock();
        lock.lock();
        try {
            File file = cacheFile(cacheName);
            Map<String, String> entries = readEntries(file);
            if (entries.remove(key) == null) return;
            writeEntries(file, entries);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean putIfAbsent(String cacheName, String key, String value) {
        return putAllIfAbsent(cacheName, Map.of(key, value)) > 0;
    }

    @Override
    public int putAllIfAbsent(String cacheName, Map<String, String> newEntries) {
        ReentrantReadWriteLock.WriteLock lock = stripeFor(cacheName).writeLock();
        lock.lock();
        try {
            File file = cacheFile(cacheName);
            Map<String, String> entries = readEntries(file);
            int written = 0;
            for (Map.Entry<String, String> entry : newEntries.entrySet()) {
                if (entries.putIfAbsent(entry.getKey(), entry.getValue()) == null) written++;
            }
            if (written > 0) writeEntries(file, entries);
            return written;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Set<String> listCacheNames() {
        File[] files = cacheDirectory().listFiles();
        Set<String> names = new TreeSet<>();
        if (files == null) return names;
        for (File file : files) {
            String fileName = file.getName();
            if (!file.isFile() || file.length() == 0) continue;
            if (!fileName.startsWith(FILE_PREFIX) || fileName.equals(KEY_FILE_NAME) || fileName.endsWith(MIGRATED_SUFFIX)) continue;
            names.add(fileName.substring(FILE_PREFIX.length()));
        }
        return names;
    }

    @Override
    public Map<String, String> entries(String cacheName) {
        ReentrantReadWriteLock.ReadLock lock = stripeFor(cacheName).readLock();
        lock.lock();
        try {
            return readEntries(cacheFile(cacheName));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void markMigrated(String cacheName) {
        ReentrantReadWriteLock.WriteLock lock = stripeFor(cacheName).writeLock();
        lock.lock();
        try {
            File file = cacheFile(cacheName);
            if (!file.exists()) return;
            File backup = new File(file.getParentFile(), file.getName() + MIGRATED_SUFFIX);
            Files.move(file.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            logger.infof("Cache file %s kept as backup: %s", file.getName(), backup.getName());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to archive migrated cache file for '" + cacheName + "': " + e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    }

    // --- entries <-> encrypted file ---

    private Map<String, String> readEntries(File file) {
        Map<String, String> entries = new LinkedHashMap<>();
        String decrypted = readAndDecrypt(file);
        if (StringUtils.isBlank(decrypted)) return entries;

        for (String line : decrypted.split("\n")) {
            if (StringUtils.isBlank(line)) continue;
            Map<String, String> mapLine = JsonUtils.fromJson(line, new TypeReference<>() {});
            String value = mapLine.get("value");
            entries.putIfAbsent(mapLine.get("key"), value == null ? "" : value);
        }
        return entries;
    }

    private void writeEntries(File file, Map<String, String> entries) {
        if (entries.isEmpty()) {
            try {
                Files.deleteIfExists(file.toPath());
            } catch (IOException e) {
                throw new IllegalStateException("Failed to delete empty cache file " + file.getName() + ": " + e.getMessage(), e);
            }
            return;
        }

        StringBuilder content = new StringBuilder();
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            content.append(JsonUtils.toJson(Map.of("key", entry.getKey(), "value", entry.getValue()))).append('\n');
        }
        encryptAndWrite(file, content.toString());
    }

    // --- Encryption helpers ---

    private String readAndDecrypt(File file) {
        if (!file.exists() || file.length() == 0) return null;
        try {
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            if (fileBytes.length <= GCM_IV_LENGTH) return null;

            byte[] iv = Arrays.copyOfRange(fileBytes, 0, GCM_IV_LENGTH);
            byte[] cipherText = Arrays.copyOfRange(fileBytes, GCM_IV_LENGTH, fileBytes.length);

            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.warn("Failed to decrypt cache file, resetting: " + e.getMessage());
            return null;
        }
    }

    private void encryptAndWrite(File file, String plainText) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            RandomHolder.INSTANCE.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey(), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] output = new byte[GCM_IV_LENGTH + cipherText.length];
            System.arraycopy(iv, 0, output, 0, GCM_IV_LENGTH);
            System.arraycopy(cipherText, 0, output, GCM_IV_LENGTH, cipherText.length);

            Files.write(file.toPath(), output);
            setRestrictedPermissions(file);
        } catch (Exception e) {
            logger.error("Failed to encrypt cache: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private SecretKey getOrCreateKey() {
        if (cachedKey != null) return cachedKey;

        synchronized (this) {
            if (cachedKey != null) return cachedKey;

            File keyFile = new File(cacheDirectory(), KEY_FILE_NAME);
            try {
                if (keyFile.exists() && keyFile.length() > 0) {
                    cachedKey = new SecretKeySpec(Files.readAllBytes(keyFile.toPath()), "AES");
                } else {
                    KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                    keyGen.init(256, RandomHolder.INSTANCE);
                    cachedKey = keyGen.generateKey();

                    Files.write(keyFile.toPath(), cachedKey.getEncoded());
                    setRestrictedPermissions(keyFile);
                }
            } catch (Exception e) {
                logger.error("Failed to load/create encryption key: " + e.getMessage(), e);
                throw new RuntimeException(e);
            }
            return cachedKey;
        }
    }

    private static void setRestrictedPermissions(File file) {
        try {
            Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-------");
            Files.setPosixFilePermissions(file.toPath(), perms);
        } catch (UnsupportedOperationException e) {
            // Windows does not support POSIX permissions, skip
        } catch (Exception e) {
            logger.warn("Could not set file permissions on: " + file.getAbsolutePath());
        }
    }

    // --- File/directory helpers ---

    private File cacheDirectory() {
        String path = baseDirectory.get() + CACHE_DIR;
        File dir = new File(path);
        if (!dir.exists() && !dir.mkdirs() && !dir.exists()) {
            logger.errorf("Could not create cache directory %s, falling back to the working directory", path);
            return new File(System.getProperty("user.dir"));
        }
        return dir;
    }

    private File cacheFile(String cacheName) {
        return new File(cacheDirectory(), FILE_PREFIX + cacheName);
    }

    private static String resolveConfiguredDirectory() {
        String cacheDir = null;
        try {
            cacheDir = ConfigProvider.getConfig().getConfigValue("cache_directory").getValue();
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
        if (StringUtils.isBlank(cacheDir)) cacheDir = System.getenv("CACHE_DIRECTORY");
        if (StringUtils.isBlank(cacheDir)) cacheDir = System.getProperty("user.dir");
        return cacheDir;
    }
}
