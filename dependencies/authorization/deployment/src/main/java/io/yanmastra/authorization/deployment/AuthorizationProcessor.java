package io.yanmastra.authorization.deployment;

import io.yanmastra.authorization.provider.ErrorMapper;
import io.yanmastra.authorization.provider.RegisterCustomizeModule;
import io.yanmastra.authorization.security.LoggingRequestFilter;
import io.yanmastra.authorization.security.UserSecurityIdentityAugmentor;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.resteasy.reactive.spi.ContainerRequestFilterBuildItem;
import io.quarkus.resteasy.reactive.spi.ExceptionMapperBuildItem;

class AuthorizationProcessor {

    private static final String FEATURE = "authorization";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    public ExceptionMapperBuildItem createErrorMapper() {
        return new ExceptionMapperBuildItem.Builder(ErrorMapper.class.getName(), Exception.class.getName())
                .setRegisterAsBean(true)
                .setPriority(1)
                .build();
    }

    @BuildStep
    public ContainerRequestFilterBuildItem createLoggingRequestFilter() {
        return new ContainerRequestFilterBuildItem.Builder(LoggingRequestFilter.class.getName())
                .setNonBlockingRequired(true)
                .setPreMatching(true)
                .setRegisterAsBean(true)
                .setPriority(1)
                .build();
    }

    @BuildStep
    public AdditionalBeanBuildItem createUserPrincipalBean() {
        return new AdditionalBeanBuildItem(UserSecurityIdentityAugmentor.class);
    }

    @BuildStep
    public AdditionalBeanBuildItem createAdditionalBeanBuildItem() {
        return new AdditionalBeanBuildItem(RegisterCustomizeModule.class);
    }
}
