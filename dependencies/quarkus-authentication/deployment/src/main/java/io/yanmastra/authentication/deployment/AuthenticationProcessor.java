package io.yanmastra.authentication.deployment;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.resteasy.reactive.spi.ContainerRequestFilterBuildItem;
import io.quarkus.resteasy.reactive.spi.ExceptionMapperBuildItem;
import io.smallrye.jwt.auth.principal.JWTCallerPrincipalFactory;
import io.yanmastra.authentication.provider.ErrorMapper;
import io.yanmastra.authentication.provider.RegisterCustomizeModule;
import io.yanmastra.authentication.security.AuthenticationService;
import io.yanmastra.authentication.security.LoggingRequestFilter;
import io.yanmastra.authentication.security.UserSecurityIdentityAugmentor;

class AuthenticationProcessor {

    private static final String FEATURE = "quarkus-authentication";

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
                .setRegisterAsBean(true)
                .setPreMatching(true)
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

    @BuildStep
    public AdditionalBeanBuildItem createJWTCallerPrincipalFactoryBean() {
        return new AdditionalBeanBuildItem(JWTCallerPrincipalFactory.class);
    }

    @BuildStep
    public AdditionalBeanBuildItem createAuthenticationServiceBean() {
        return new AdditionalBeanBuildItem(AuthenticationService.class);
    }
}
