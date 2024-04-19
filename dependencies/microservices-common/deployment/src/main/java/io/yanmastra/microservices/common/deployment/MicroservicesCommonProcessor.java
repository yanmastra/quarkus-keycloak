package io.yanmastra.microservices.common.deployment;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.yanmastra.commonClass.utils.CrudQueryFilterUtils;
import io.yanmastra.microservices.common.BeanProvider;

class MicroservicesCommonProcessor {

    private static final String FEATURE = "microservices-common";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem createBeanObjects() {
        return new AdditionalBeanBuildItem(BeanProvider.class, CrudQueryFilterUtils.class);
    }
}
