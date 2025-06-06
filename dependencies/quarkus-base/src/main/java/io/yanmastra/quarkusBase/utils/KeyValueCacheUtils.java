package io.yanmastra.quarkusBase.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.json.Json;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

public class KeyValueCacheUtils {
    private static final String CACHE_DIR = "/.cache_v2";

    private static final Logger logger = Logger.getLogger(KeyValueCacheUtils.class.getName());

    public static synchronized void removeCache(String cacheName, String key) {
        saveCache(cacheName, key, "");
    }

    public static synchronized void saveCache(String cacheName, String key, String value) {
        if (StringUtils.isBlank(key))
            throw new IllegalArgumentException("key can't be empty");

        if (StringUtils.isBlank(value)) value = "";

        Map<String, String> mapLine = Map.of("key", key, "value", value);
        String sLine = JsonUtils.toJson(mapLine);

        File file = getCacheFileName(cacheName);
        StringBuilder cache = new StringBuilder();
        Set<String> usedKey = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            boolean hasReplaced = false;
            while ((line = reader.readLine()) != null) {
                if (StringUtils.isBlank(line)) continue;
                Map<String, String> mapLine1 = JsonUtils.fromJson(line, new TypeReference<>() {
                });
                String cKey = mapLine1.get("key");
                if (usedKey.contains(cKey)) continue;
                usedKey.add(cKey);

                if(cKey.equals(key)) {
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
        } catch (IOException ioe) {
            logger.error(ioe.getMessage(), ioe);
            throw new RuntimeException(ioe);
        }

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(cache.toString());
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String findCache(String cacheName, String key) {
        File file = getCacheFileName(cacheName);

        String sKey = JsonUtils.toJson(Map.of("key", key));
        sKey = sKey.substring(1, sKey.length()-1);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (StringUtils.isNotBlank(line) && line.contains(sKey)) {
                    Map<String, String> mapLine = JsonUtils.fromJson(line, new TypeReference<>() {
                    });
                    return mapLine.get("value");
                }
            }

            reader.close();
            return null;
        } catch (IOException e) {
            throw new RuntimeException("Error finding cache:"+file.getAbsolutePath()+", cache:"+cacheName+"/"+key, e);
        }
    }

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
                        boolean subResult = root.mkdir();
                    }
                } catch (Exception e){
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
        }catch (Exception e) {
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
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return file;
    }

    public static String hide(String s) {
        if (StringUtils.isBlank(s)) return null;
        return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    public static String showHiddenString(String s) {
        return new String(Base64.getDecoder().decode(s));
    }

    public static void main(String[] args) {
        test();
    }

    public static void test() {
        Random random = new Random();
        long maxWrite = 0L;
        long maxRead = 0L;
        long maxUpdate = 0L;
        long maxRemove = 0L;
        String longestValue = null;
        Set<String> keys = new HashSet<>();
        String cacheName = "TEST_CACHE_3";
        for (int i = 0; i < 2000; i++) {
            String key = UUID.randomUUID().toString();
            String val = Json.createObjectBuilder()
                    .add("a", random.nextInt(99999999)+"")
                    .add("b", random.nextInt(99999999)+"")
                    .add("c", random.nextInt(99999999)+"")
                    .add("d", random.nextInt(99999999)+"")
                    .add(UUID.randomUUID().toString(), random.nextInt(99999999)+"")
                    .add(random.nextInt(99999999)+"", UUID.randomUUID().toString())
                    .build().toString();

            Instant instant = Instant.now();
            saveCache(cacheName, key, val);
            long take = Instant.now().toEpochMilli() - instant.toEpochMilli();
            if (maxWrite < take) maxWrite = take;
            keys.add(key);
        }

        for (String key : keys) {
            String val = Json.createObjectBuilder()
                    .add("a", random.nextInt(99999999)+"")
                    .add("b", random.nextInt(99999999)+"")
                    .add("c", random.nextInt(99999999)+"")
                    .add("d", random.nextInt(99999999)+"")
                    .add(UUID.randomUUID().toString(), random.nextInt(99999999)+"")
                    .add(random.nextInt(99999999)+"", UUID.randomUUID().toString())
                    .build().toString();

            Instant instant = Instant.now();
            saveCache(cacheName, key, val);
            long take = Instant.now().toEpochMilli() - instant.toEpochMilli();
            if (maxUpdate < take) maxUpdate = take;

            instant = Instant.now();
            String value = findCache(cacheName, key);
            take = Instant.now().toEpochMilli() - instant.toEpochMilli();
            if (maxRead < take) {
                maxRead = take;
                longestValue = value;
            }

            instant = Instant.now();
            removeCache(cacheName, key);
            take = Instant.now().toEpochMilli() - instant.toEpochMilli();
            if (maxRemove < take) maxRemove = take;

            value = findCache(cacheName, key);
            if (StringUtils.isBlank(value)) {
                System.out.println("failed to remove " + key);
            }
        }

        System.out.println("max write take time:"+maxWrite+"ms");
        System.out.println("max update take time:"+maxUpdate+"ms");
        System.out.println("max read take time:"+maxRead+"ms, val:"+longestValue);
        System.out.println("max remove take time:"+maxRemove+"ms");
    }


}
