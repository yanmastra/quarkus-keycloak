package io.yanmastra.quarkus.mediafilemanager;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "media")
public interface MediaFileManagerConfig {

    /**
     * Storage backend: {@code local} (default) or {@code s3}.
     */
    @WithName("storage.type")
    @WithDefault("local")
    String storageType();

    /**
     * Local filesystem path for public media files. Used when {@code media.storage.type=local}.
     */
    @WithDefault("/tmp/media")
    String path();

    /**
     * Secured media configuration.
     */
    Secured secured();

    /**
     * S3 / MinIO configuration.
     */
    S3 s3();

    interface Secured {
        /**
         * Local filesystem path for secured (private) media files. Used when {@code media.storage.type=local}.
         */
        @WithDefault("/tmp/secured-media")
        String path();
    }

    interface S3 {
        /**
         * Name of the single S3 bucket used for both public and secured media.
         * Public files are stored under the {@code media/} prefix; secured files under {@code secured-media/}.
         */
        @WithDefault("media")
        String bucket();
    }
}
