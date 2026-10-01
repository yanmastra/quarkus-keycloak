package io.yanmastra.quarkus.rediscache.deployment;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.yanmastra.quarkus.rediscache.RedisKeyValueCacheStore;

class RedisCacheProcessor {

    private static final String FEATURE = "redis-cache";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem registerStore() {
        // nothing injects the store: it registers itself at start-up, so it must not be removed as unused
        return AdditionalBeanBuildItem.unremovableOf(RedisKeyValueCacheStore.class);
    }
}
