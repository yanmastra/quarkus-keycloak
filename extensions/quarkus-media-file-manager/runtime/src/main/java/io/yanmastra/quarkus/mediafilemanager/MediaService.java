package io.yanmastra.quarkus.mediafilemanager;

import com.fasterxml.jackson.core.type.TypeReference;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.yanmastra.quarkusBase.utils.JsonUtils;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.imgscalr.Scalr;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static io.yanmastra.quarkus.mediafilemanager.MediaUtils.*;


@ApplicationScoped
public class MediaService {

    private static final Log log = LogFactory.getLog(MediaService.class);

    @Inject
    MediaFileManagerConfig config;

    @Inject
    Instance<S3Client> s3ClientInstance;

    private boolean isS3() {
        return "s3".equalsIgnoreCase(config.storageType());
    }

    @PostConstruct
    void init() {
        if (!isS3()) return;
        S3Client client = s3ClientInstance.get();
        String bucket = config.s3().bucket();
        try {
            client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
            log.info("S3 bucket already exists: " + bucket);
        } catch (NoSuchBucketException e) {
            client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
            log.info("Created S3 bucket: " + bucket);
        } catch (Exception e) {
            log.warn("Could not verify/create S3 bucket '" + bucket + "': " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Store methods — public API unchanged
    // -------------------------------------------------------------------------

    public ImageStore storeImage(File file) {
        if (isS3()) return new ImageStore(file, s3ClientInstance.get(), config.s3().bucket(), BASE_MEDIA_PATH);
        return new ImageStore(file, config.path(), BASE_MEDIA_PATH);
    }

    public ImageStore storeSecuredImage(File file) {
        if (isS3()) return new ImageStore(file, s3ClientInstance.get(), config.s3().bucket(), BASE_SECURED_MEDIA_PATH);
        return new ImageStore(file, config.secured().path(), BASE_SECURED_MEDIA_PATH);
    }

    public FileStore storeFile(File file) {
        if (isS3()) return new FileStore(file, s3ClientInstance.get(), config.s3().bucket(), BASE_MEDIA_PATH);
        return new FileStore(file, config.path(), BASE_MEDIA_PATH);
    }

    public FileStore storeSecuredFile(File file) {
        if (isS3()) return new FileStore(file, s3ClientInstance.get(), config.s3().bucket(), BASE_SECURED_MEDIA_PATH);
        return new FileStore(file, config.secured().path(), BASE_SECURED_MEDIA_PATH);
    }

    // -------------------------------------------------------------------------
    // Load methods
    // -------------------------------------------------------------------------

    public void loadMedia(String fileName, boolean isDownload, ContainerResponseContext responseContext) {
        if (isS3()) {
            loadFromS3ToContext(config.s3().bucket(), BASE_MEDIA_PATH, fileName, isDownload, responseContext);
            return;
        }
        File file = resolveSecureFile(config.path(), fileName);
        try (InputStream is = new BufferedInputStream(new FileInputStream(file));
             OutputStream os = responseContext.getEntityStream()) {
            os.write(is.readAllBytes());
            MediaUtils.createFileResponse(responseContext, fileName, isDownload);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public Response loadMedia(String fileName, boolean isDownload) {
        if (isS3()) return loadFromS3(config.s3().bucket(), BASE_MEDIA_PATH, fileName, isDownload);
        return loadMediaToResponse(fileName, isDownload, config.path());
    }

    public Response loadSecuredMedia(String fileName, boolean isDownload) {
        if (isS3()) return loadFromS3(config.s3().bucket(), BASE_SECURED_MEDIA_PATH, fileName, isDownload);
        return loadMediaToResponse(fileName, isDownload, config.secured().path());
    }

    public Response loadMediaById(String fileId, String size, boolean isDownload) {
        if (isS3()) return loadFromS3ById(config.s3().bucket(), BASE_MEDIA_PATH, fileId, size, isDownload);
        return loadLocalById(config.path(), fileId, size, isDownload);
    }

    public Response loadSecuredMediaById(String fileId, String size, boolean isDownload) {
        if (isS3()) return loadFromS3ById(config.s3().bucket(), BASE_SECURED_MEDIA_PATH, fileId, size, isDownload);
        return loadLocalById(config.secured().path(), fileId, size, isDownload);
    }

    // -------------------------------------------------------------------------
    // Remove methods
    // -------------------------------------------------------------------------

    public void removeMedia(String fileId) {
        if (isS3()) { removeFromS3(config.s3().bucket(), BASE_MEDIA_PATH, fileId); return; }
        removeLocal(config.path(), fileId);
    }

    public void removeSecuredMedia(String fileId) {
        if (isS3()) { removeFromS3(config.s3().bucket(), BASE_SECURED_MEDIA_PATH, fileId); return; }
        removeLocal(config.secured().path(), fileId);
    }

    // -------------------------------------------------------------------------
    // Private S3 helpers
    // -------------------------------------------------------------------------

    private Response loadFromS3(String bucket, String urlPath, String fileName, boolean isDownload) {
        String key = s3KeyPrefix(urlPath) + "/" + fileName;
        try {
            byte[] data = s3ClientInstance.get()
                    .getObjectAsBytes(GetObjectRequest.builder().bucket(bucket).key(key).build())
                    .asByteArray();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(data);
            return MediaUtils.createFileResponse(baos, fileName, isDownload);
        } catch (NoSuchKeyException e) {
            throw new NotFoundException(fileName);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Response loadFromS3ById(String bucket, String urlPath, String fileId, String size, boolean isDownload) {
        String prefix = s3KeyPrefix(urlPath);
        String metaKey = prefix + "/meta/" + fileId + ".json";
        try {
            String metaJson = s3ClientInstance.get()
                    .getObjectAsBytes(GetObjectRequest.builder().bucket(bucket).key(metaKey).build())
                    .asUtf8String();
            List<String> paths = JsonUtils.fromJson(metaJson, new TypeReference<>() {});
            String relPath = findPathBySize(paths, size);
            if (relPath == null) throw new NotFoundException("No variant '" + size + "' found for file " + fileId);
            String fileName = relPath.startsWith("/") ? relPath.substring(1) : relPath;
            return loadFromS3(bucket, urlPath, fileName, isDownload);
        } catch (NotFoundException e) {
            throw e;
        } catch (NoSuchKeyException e) {
            throw new NotFoundException(fileId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Response loadLocalById(String basePath, String fileId, String size, boolean isDownload) {
        String metaPath = (basePath.endsWith("/") ? basePath : basePath + "/") + "meta/" + fileId + ".json";
        File metaFile = new File(metaPath);
        if (!metaFile.exists()) throw new NotFoundException(fileId);
        try (InputStream is = new FileInputStream(metaFile)) {
            List<String> paths = JsonUtils.fromJson(new String(is.readAllBytes()), new TypeReference<>() {});
            String relPath = findPathBySize(paths, size);
            if (relPath == null) throw new NotFoundException("No variant '" + size + "' found for file " + fileId);
            String fileName = relPath.startsWith("/") ? relPath.substring(1) : relPath;
            return loadMediaToResponse(fileName, isDownload, basePath);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static String findPathBySize(List<String> paths, String size) {
        if (StringUtils.isBlank(size)) {
            return paths.stream().filter(p -> !p.matches(".*/w\\d+/.*")).findFirst().orElse(null);
        }
        String segment = "/" + size + "/";
        return paths.stream().filter(p -> p.contains(segment)).findFirst().orElse(null);
    }

    private void loadFromS3ToContext(String bucket, String urlPath, String fileName, boolean isDownload,
                                     ContainerResponseContext responseContext) {
        String key = s3KeyPrefix(urlPath) + "/" + fileName;
        try {
            byte[] data = s3ClientInstance.get()
                    .getObjectAsBytes(GetObjectRequest.builder().bucket(bucket).key(key).build())
                    .asByteArray();
            try (OutputStream os = responseContext.getEntityStream()) {
                os.write(data);
                MediaUtils.createFileResponse(responseContext, fileName, isDownload);
            }
        } catch (NoSuchKeyException e) {
            throw new NotFoundException(fileName);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void removeFromS3(String bucket, String urlPath, String fileId) {
        String prefix = s3KeyPrefix(urlPath);
        String metaKey = prefix + "/meta/" + fileId + ".json";
        S3Client client = s3ClientInstance.get();
        try {
            String metaJson = client.getObjectAsBytes(
                    GetObjectRequest.builder().bucket(bucket).key(metaKey).build()
            ).asUtf8String();
            List<String> paths = JsonUtils.fromJson(metaJson, new TypeReference<>() {});
            for (String path : paths) {
                String key = prefix + path;
                try {
                    client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
                    log.info("Deleted S3 object: " + key);
                } catch (Exception e) {
                    log.warn("Failed to delete S3 object: " + key + " - " + e.getMessage());
                }
            }
            client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(metaKey).build());
            log.info("Deleted S3 meta: " + metaKey);
        } catch (NoSuchKeyException e) {
            log.warn("Meta not found in S3: " + metaKey);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Private local helpers
    // -------------------------------------------------------------------------

    private static File resolveSecureFile(String basePath, String fileName) {
        basePath = StringUtils.isNotBlank(basePath) && basePath.endsWith("/") ? basePath : basePath + "/";
        Path base = Paths.get(basePath).toAbsolutePath().normalize();
        Path resolved = base.resolve(fileName).normalize();
        if (!resolved.startsWith(base)) {
            throw new BadRequestException("Invalid file path");
        }
        return resolved.toFile();
    }

    private static Response loadMediaToResponse(String fileName, boolean isDownload, String basePath) {
        File file = resolveSecureFile(basePath, fileName);
        if (!file.exists()) throw new NotFoundException(fileName);
        try (InputStream is = new BufferedInputStream(new FileInputStream(file));
             ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            os.write(is.readAllBytes());
            return MediaUtils.createFileResponse(os, fileName, isDownload);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static void removeLocal(String basePath, String fileId) {
        basePath = StringUtils.isNotBlank(basePath) && basePath.endsWith("/")
                ? basePath.substring(0, basePath.length() - 1) : basePath;
        String metaPath = basePath + META_PATH + "/" + fileId + ".json";
        File metaFile = new File(metaPath);
        if (metaFile.exists()) {
            boolean result;
            try (InputStream is = new FileInputStream(metaFile)) {
                List<String> files = JsonUtils.fromJson(new String(is.readAllBytes()), new TypeReference<>() {});
                for (String f : files) {
                    File media = new File(basePath + f);
                    if (media.exists()) {
                        result = media.delete();
                        log.info("file: " + media.getAbsolutePath() + " tried to be deleted, result: " + result);
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            result = metaFile.delete();
            log.info("file: " + metaFile.getAbsolutePath() + " tried to be deleted, result: " + result);
        }
    }

    private static void saveMetaLocal(String metaPath, String baseFileName, List<String> paths) {
        metaPath = metaPath.endsWith("/") ? metaPath : metaPath + "/";
        File metaFile = new File(metaPath + baseFileName + ".json");
        try (OutputStream os = new FileOutputStream(metaFile)) {
            os.write(JsonUtils.toJson(paths).getBytes());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

static String s3KeyPrefix(String urlPath) {
        return urlPath.startsWith("/") ? urlPath.substring(1) : urlPath;
    }

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS =
            Set.of("png", "jpg", "jpeg", "gif", "webp", "bmp", "svg");

    // -------------------------------------------------------------------------
    // Inner builder classes
    // -------------------------------------------------------------------------

    @RegisterForReflection
    public static class MediaStore {
        protected File file;
        protected String path;
        protected String urlPath;
        protected String specificLocation;
        // S3 fields (null when using local storage)
        protected S3Client s3Client;
        protected String bucket;

        public Map<String, String> store() {
            throw new IllegalStateException("Not implemented");
        }

        @SuppressWarnings("unchecked")
        public <E extends MediaStore> E specificLocation(String additionalPath) {
            if (StringUtils.isNotBlank(additionalPath)) {
                this.specificLocation = additionalPath
                        .replaceAll("/+", "/")   // collapse consecutive slashes
                        .replaceAll("^/+|/+$", ""); // strip leading and trailing slashes
            }
            return (E) this;
        }

        protected boolean isS3() {
            return s3Client != null;
        }

        protected String buildS3Prefix() {
            String prefix = s3KeyPrefix(urlPath);
            if (StringUtils.isNotBlank(specificLocation)) {
                prefix += "/" + specificLocation;
            }
            return prefix;
        }

        protected String buildLocalBasePath() {
            String base = path.endsWith("/") ? path : path + "/";
            if (StringUtils.isNotBlank(specificLocation)) {
                base += specificLocation + "/";
            }
            return base;
        }
    }

    @RegisterForReflection
    public static class ImageStore extends MediaStore {

        private final List<Integer> widthVariant;

        public ImageStore(File file, String path, String urlPath) {
            this.file = file;
            this.path = path;
            this.urlPath = urlPath;
            widthVariant = new ArrayList<>();
        }

        public ImageStore(File file, S3Client s3Client, String bucket, String urlPath) {
            this.file = file;
            this.s3Client = s3Client;
            this.bucket = bucket;
            this.urlPath = urlPath;
            widthVariant = new ArrayList<>();
        }

        public ImageStore addWidthVariant(int width) {
            widthVariant.add(width);
            return this;
        }

        @Override
        @Nullable
        public Map<String, String> store() {
            return isS3() ? storeToS3() : storeToLocal();
        }

        private Map<String, String> storeToLocal() {
            if (file == null) throw new IllegalArgumentException("File should not be null.");
            if (!file.exists()) throw new NotFoundException("File does not exist: " + file.getAbsolutePath());

            log.debug("file:" + file.getAbsolutePath());
            try (InputStream is = new FileInputStream(file)) {
                String basePath = buildLocalBasePath();
                // meta always at root (no specificLocation) so removeMedia can find it by fileId alone
                String rootMetaPath = (path.endsWith("/") ? path : path + "/") + "meta";

                Files.createDirectories(Paths.get(basePath));
                Files.createDirectories(Paths.get(rootMetaPath));

                String baseFileName = UUID.randomUUID().toString();
                String extension = extractExtension(file.getAbsolutePath());

                if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase())) {
                    throw new BadRequestException("File type not allowed: " + extension);
                }

                BufferedImage bufferedImage = ImageIO.read(is);
                if (bufferedImage == null) throw new BadRequestException("Invalid image file: could not decode image content");

                float aspectRatio = (float) bufferedImage.getWidth() / bufferedImage.getHeight();
                Map<String, String> images = new HashMap<>();
                List<String> filePaths = new ArrayList<>();

                // prefix relative to `path` root; includes specificLocation so loadMedia can resolve it
                String locPrefix = StringUtils.isNotBlank(specificLocation) ? "/" + specificLocation : "";

                String oriFileName = baseFileName + "." + extension;
                ImageIO.write(bufferedImage, extension, new File(basePath + oriFileName));
                images.put(DEFAULT_FILE, urlPath + locPrefix + "/" + oriFileName);
                images.put(FILE_ID, baseFileName);
                filePaths.add(locPrefix + "/" + oriFileName);

                for (int w : widthVariant) {
                    int tw = aspectRatio >= 1 ? w : (int) (w * aspectRatio);
                    int th = aspectRatio >= 1 ? (int) (w / aspectRatio) : w;
                    BufferedImage resized = Scalr.resize(bufferedImage, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, tw, th);
                    String variantDir = basePath + "w" + w;
                    Files.createDirectories(Paths.get(variantDir));
                    ImageIO.write(resized, extension, new File(variantDir + "/" + oriFileName));
                    images.put("w" + w, urlPath + locPrefix + "/w" + w + "/" + oriFileName);
                    filePaths.add(locPrefix + "/w" + w + "/" + oriFileName);
                }

                saveMetaLocal(rootMetaPath, baseFileName, filePaths);
                return images;
            } catch (BadRequestException | NotFoundException e) {
                throw e;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return null;
            }
        }

        private Map<String, String> storeToS3() {
            if (file == null || !file.exists()) throw new NotFoundException("File not found");

            String extension = extractExtension(file.getName());
            if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase())) {
                throw new BadRequestException("File type not allowed: " + extension);
            }

            try (InputStream is = new FileInputStream(file)) {
                BufferedImage bufferedImage = ImageIO.read(is);
                if (bufferedImage == null) throw new BadRequestException("Invalid image file");

                float aspectRatio = (float) bufferedImage.getWidth() / bufferedImage.getHeight();
                String baseFileName = UUID.randomUUID().toString();
                String s3Prefix = buildS3Prefix(); // includes specificLocation, e.g. "media/mastra/wayan"
                String s3BasePrefix = s3KeyPrefix(urlPath); // without specificLocation, e.g. "media"
                String locPrefix = StringUtils.isNotBlank(specificLocation) ? "/" + specificLocation : "";

                Map<String, String> images = new HashMap<>();
                List<String> keyPaths = new ArrayList<>();

                String oriFileName = baseFileName + "." + extension;
                putImageToS3(bufferedImage, extension, s3Prefix + "/" + oriFileName);
                images.put(DEFAULT_FILE, urlPath + locPrefix + "/" + oriFileName);
                images.put(FILE_ID, baseFileName);
                keyPaths.add(locPrefix + "/" + oriFileName);

                for (int w : widthVariant) {
                    int tw = aspectRatio >= 1 ? w : (int) (w * aspectRatio);
                    int th = aspectRatio >= 1 ? (int) (w / aspectRatio) : w;
                    BufferedImage resized = Scalr.resize(bufferedImage, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, tw, th);
                    String variantKey = s3Prefix + "/w" + w + "/" + oriFileName;
                    putImageToS3(resized, extension, variantKey);
                    images.put("w" + w, urlPath + locPrefix + "/w" + w + "/" + oriFileName);
                    keyPaths.add(locPrefix + "/w" + w + "/" + oriFileName);
                }

                // meta at root (no specificLocation) so removeMedia can find it by fileId alone
                putJsonToS3(s3BasePrefix + "/meta/" + baseFileName + ".json", JsonUtils.toJson(keyPaths));
                return images;
            } catch (BadRequestException | NotFoundException e) {
                throw e;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return null;
            }
        }

        private void putImageToS3(BufferedImage image, String format, String key) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, format, baos);
            byte[] data = baos.toByteArray();
            s3Client.putObject(
                    PutObjectRequest.builder().bucket(bucket).key(key).contentLength((long) data.length).build(),
                    RequestBody.fromBytes(data));
        }

        private void putJsonToS3(String key, String json) {
            byte[] data = json.getBytes();
            s3Client.putObject(
                    PutObjectRequest.builder().bucket(bucket).key(key)
                            .contentType("application/json").contentLength((long) data.length).build(),
                    RequestBody.fromBytes(data));
        }
    }

    @RegisterForReflection
    public static class FileStore extends MediaStore {

        public FileStore(File file, String path, String urlPath) {
            this.file = file;
            this.path = path;
            this.urlPath = urlPath;
        }

        public FileStore(File file, S3Client s3Client, String bucket, String urlPath) {
            this.file = file;
            this.s3Client = s3Client;
            this.bucket = bucket;
            this.urlPath = urlPath;
        }

        @Override
        public Map<String, String> store() {
            return isS3() ? storeToS3() : storeToLocal();
        }

        private Map<String, String> storeToLocal() {
            if (file == null || !file.exists())
                throw new IllegalArgumentException("File must not be null or missing");

            try {
                String basePath = buildLocalBasePath();
                String rootMetaPath = (path.endsWith("/") ? path : path + "/") + "meta";
                Files.createDirectories(Paths.get(basePath));
                Files.createDirectories(Paths.get(rootMetaPath));

                String extension = extractExtension(file.getName());
                String fileId = UUID.randomUUID().toString();
                String storedName = fileId + (extension.isEmpty() ? "" : "." + extension);
                Files.copy(file.toPath(), Paths.get(basePath + storedName));

                String locPrefix = StringUtils.isNotBlank(specificLocation) ? "/" + specificLocation : "";
                String relPath = locPrefix + "/" + storedName;
                saveMetaLocal(rootMetaPath, fileId, List.of(relPath));

                return Map.of(FILE_ID, fileId, DEFAULT_FILE, urlPath + relPath);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return null;
            }
        }

        private Map<String, String> storeToS3() {
            if (file == null || !file.exists())
                throw new IllegalArgumentException("File must not be null or missing");

            try {
                String extension = extractExtension(file.getName());
                String fileId = UUID.randomUUID().toString();
                String storedName = fileId + (extension.isEmpty() ? "" : "." + extension);
                String s3Prefix = buildS3Prefix(); // includes specificLocation
                String s3BasePrefix = s3KeyPrefix(urlPath); // without specificLocation
                String fileKey = s3Prefix + "/" + storedName;

                byte[] data = Files.readAllBytes(file.toPath());
                s3Client.putObject(
                        PutObjectRequest.builder().bucket(bucket).key(fileKey).contentLength((long) data.length).build(),
                        RequestBody.fromBytes(data));

                String locPrefix = StringUtils.isNotBlank(specificLocation) ? "/" + specificLocation : "";
                String relPath = locPrefix + "/" + storedName;
                // meta at root (no specificLocation) so removeMedia can find it by fileId alone
                String metaKey = s3BasePrefix + "/meta/" + fileId + ".json";
                byte[] metaBytes = JsonUtils.toJson(List.of(relPath)).getBytes();
                s3Client.putObject(
                        PutObjectRequest.builder().bucket(bucket).key(metaKey)
                                .contentType("application/json").contentLength((long) metaBytes.length).build(),
                        RequestBody.fromBytes(metaBytes));

                return Map.of(FILE_ID, fileId, DEFAULT_FILE, urlPath + relPath);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return null;
            }
        }
    }

    private static String extractExtension(String fileName) {
        if (!fileName.contains(".")) return "";
        String ext = fileName.substring(fileName.lastIndexOf(".") + 1);
        return ext.length() <= 5 ? ext : "";
    }
}
