package io.yanmastra.microservices.common.deployment;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.yanmastra.microservices.common.BeanProvider;
import io.yanmastra.securedMessaging.serialiDeserializer.SecureMsgDeserializer;
import io.yanmastra.securedMessaging.serialiDeserializer.SecureMsgSerializer;

class MicroservicesCommonProcessor {

    private static final String FEATURE = "microservices-common";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem createObjectMapper() {
        return new AdditionalBeanBuildItem(BeanProvider.class);
    }

    @BuildStep
    AdditionalBeanBuildItem createMessagingSerialDeserializer() {
        return new AdditionalBeanBuildItem(SecureMsgDeserializer.class, SecureMsgSerializer.class);
    }
}
