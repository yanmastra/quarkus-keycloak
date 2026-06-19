package io.yanmastra.quarkus.mediafilemanager.deployment;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.yanmastra.quarkus.mediafilemanager.MediaResource;
import io.yanmastra.quarkus.mediafilemanager.MediaService;
import io.yanmastra.quarkus.mediafilemanager.SecuredMediaResource;

class QuarkusMediaFileManagerProcessor {

    private static final String FEATURE = "quarkus-media-file-manager";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem registerBeans() {
        return AdditionalBeanBuildItem.unremovableOf(MediaService.class);
    }

    @BuildStep
    AdditionalBeanBuildItem registerResources() {
        return new AdditionalBeanBuildItem(MediaResource.class, SecuredMediaResource.class);
    }

    @BuildStep
    ReflectiveClassBuildItem registerForReflection() {
        return ReflectiveClassBuildItem.builder(
                MediaService.MediaStore.class,
                MediaService.ImageStore.class,
                MediaService.FileStore.class
        ).methods().fields().build();
    }
}
