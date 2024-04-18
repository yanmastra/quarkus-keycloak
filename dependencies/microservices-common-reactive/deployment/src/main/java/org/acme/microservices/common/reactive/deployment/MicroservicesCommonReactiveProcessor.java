package org.acme.microservices.common.reactive.deployment;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import org.acme.microservices.common.reactive.BeanProvider;

class MicroservicesCommonReactiveProcessor {

    private static final String FEATURE = "microservices-common-reactive";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem createObjectMapper() {
        return new AdditionalBeanBuildItem(BeanProvider.class);
    }
}
