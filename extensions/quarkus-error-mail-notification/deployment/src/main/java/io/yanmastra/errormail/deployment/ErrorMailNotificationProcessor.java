package io.yanmastra.errormail.deployment;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.resteasy.reactive.spi.ExceptionMapperBuildItem;
import io.yanmastra.errormail.DefaultCriticalErrorFilter;
import io.yanmastra.errormail.ErrorMailNotifier;
import io.yanmastra.quarkusBase.provider.ErrorMapper;

class ErrorMailNotificationProcessor {

    private static final String FEATURE = "quarkus-error-mail-notification";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    public AdditionalBeanBuildItem registerErrorMailNotifier() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClasses(ErrorMailNotifier.class, DefaultCriticalErrorFilter.class)
                .setUnremovable()
                .build();
    }

    @BuildStep
    public ExceptionMapperBuildItem createErrorMapper() {
        return new ExceptionMapperBuildItem.Builder(ErrorMapper.class.getName(), Exception.class.getName())
                .setRegisterAsBean(true)
                .build();
    }
}
