package io.yanmastra.quarkusBase.utils;

import com.fasterxml.jackson.core.type.TypeReference;
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
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class KeyValueCacheUtils {
    private static final String CACHE_DIR = "/.cache_v2";

    private static final Logger logger = Logger.getLogger(KeyValueCacheUtils.class.getName());

    private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    private static final String KEY_FILE_NAME = ".cache.key";
    private static final String LOCK_SUFFIX = ".lock";

    private static SecretKey cachedKey = null;

    public static void removeCache(String cacheName, String key) {
        saveCache(cacheName, key, "");
    }

    public static void saveCache(String cacheName, String key, String value) {
        if (StringUtils.isBlank(key))
            throw new IllegalArgumentException("key can't be empty");

        if (StringUtils.isBlank(value)) value = "";

        Map<String, String> mapLine = Map.of("key", key, "value", value);
        String sLine = JsonUtils.toJson(mapLine);

        File file = getCacheFileName(cacheName);
        File lockFile = new File(file.getAbsolutePath() + LOCK_SUFFIX);

        try (FileChannel channel = FileChannel.open(lockFile.toPath(),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             FileLock lock = channel.lock()) {

            StringBuilder cache = new StringBuilder();
            Set<String> usedKey = new HashSet<>();

            String decrypted = readAndDecrypt(file);
            if (StringUtils.isNotBlank(decrypted)) {
                boolean hasReplaced = false;
                for (String line : decrypted.split("\n")) {
                    if (StringUtils.isBlank(line)) continue;
                    Map<String, String> mapLine1 = JsonUtils.fromJson(line, new TypeReference<>() {
                    });
                    String cKey = mapLine1.get("key");
                    if (usedKey.contains(cKey)) continue;
                    usedKey.add(cKey);

                    if (cKey.equals(key)) {
                        if (StringUtils.isNotBlank(value)) {
                            cache.append(sLine);
                            cache.append('\n');
                        }
                        hasReplaced = true;
                    } else {
                        cache.append(line);
                        cache.append('\n');
                    }
                }

                if (!hasReplaced) {
                    cache.append(sLine);
                    cache.append('\n');
                }
            } else {
                cache.append(sLine);
                cache.append('\n');
            }

            encryptAndWrite(file, cache.toString());
        } catch (IOException e) {
            logger.error("Failed to acquire lock for cache: " + cacheName, e);
            throw new RuntimeException(e);
        }
    }

    public static String findCache(String cacheName, String key) {
        File file = getCacheFileName(cacheName);
        File lockFile = new File(file.getAbsolutePath() + LOCK_SUFFIX);

        try (FileChannel channel = FileChannel.open(lockFile.toPath(),
                StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
             FileLock lock = channel.lock(0, Long.MAX_VALUE, true)) {

            String decrypted = readAndDecrypt(file);
            if (StringUtils.isBlank(decrypted)) return null;

            for (String line : decrypted.split("\n")) {
                if (StringUtils.isBlank(line)) continue;
                Map<String, String> mapLine = JsonUtils.fromJson(line, new TypeReference<>() {
                });
                if (key.equals(mapLine.get("key"))) {
                    return mapLine.get("value");
                }
            }
            return null;
        } catch (IOException e) {
            logger.error("Failed to acquire lock for cache: " + cacheName, e);
            throw new RuntimeException(e);
        }
    }

    // --- Encryption helpers ---

    private static String readAndDecrypt(File file) {
        if (!file.exists() || file.length() == 0) return null;
        try {
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            if (fileBytes.length <= GCM_IV_LENGTH) return null;

            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] cipherText = new byte[fileBytes.length - GCM_IV_LENGTH];
            System.arraycopy(fileBytes, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(fileBytes, GCM_IV_LENGTH, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), spec);

            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.warn("Failed to decrypt cache file, resetting: " + e.getMessage());
            return null;
        }
    }

    private static void encryptAndWrite(File file, String plainText) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            PasswordGenerator.random().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey(), spec);

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

    private static SecretKey getOrCreateKey() {
        if (cachedKey != null) return cachedKey;

        synchronized (KeyValueCacheUtils.class) {
            if (cachedKey != null) return cachedKey;

            String cacheDir = getCacheDir();
            File dir = checkPath(cacheDir + CACHE_DIR);
            File keyFile = new File(dir, KEY_FILE_NAME);

            try {
                if (keyFile.exists() && keyFile.length() > 0) {
                    byte[] keyBytes = Files.readAllBytes(keyFile.toPath());
                    cachedKey = new SecretKeySpec(keyBytes, "AES");
                } else {
                    KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                    keyGen.init(256, PasswordGenerator.random());
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

    private static File checkPath(String path) {
        File file = new File(path);
        if (!file.exists()) {
            boolean result = file.mkdir();
            if (!result) {
                try {
                    String[] pathSegment = path.split("/");
                    File root = null;
                    for (String segment : pathSegment) {
                        if (root == null) {
                            if (path.startsWith("/"))
                                root = new File("/" + segment);
                            else
                                root = new File(segment);
                        } else {
                            root = new File(root, segment);
                        }
                        if (root.exists()) continue;
                        root.mkdir();
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    file = new File(System.getProperty("user.dir"));
                }
            }
        }
        return file;
    }

    private static String getCacheDir() {
        String cacheDir = null;
        try {
            cacheDir = ConfigProvider.getConfig().getConfigValue("cache_directory").getValue();
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
        if (StringUtils.isBlank(cacheDir)) cacheDir = System.getenv("CACHE_DIRECTORY");
        if (StringUtils.isBlank(cacheDir)) cacheDir = System.getenv("user.dir");
        return cacheDir;
    }

    private static File getCacheFileName(String cacheName) {
        String cacheFileName = ".cache." + cacheName;
        File dir = checkPath(getCacheDir() + CACHE_DIR);
        File file = new File(dir, cacheFileName);
        if (!file.exists()) {
            try {
                boolean result = file.createNewFile();
                if (result) setRestrictedPermissions(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return file;
    }
}
