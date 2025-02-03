package io.onebyone.authorization.deployment;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.onebyone.authorization.security.UserSecurityIdentityAugmentor;

class AuthorizationProcessor {

    private static final String FEATURE = "quarkus-authorization";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }
//
//    @BuildStep
//    public ExceptionMapperBuildItem createErrorMapper() {
//        return new ExceptionMapperBuildItem.Builder(ErrorMapper.class.getName(), Exception.class.getName())
//                .setRegisterAsBean(true)
//                .setPriority(1)
//                .build();
//    }
//
//    @BuildStep
//    public ContainerResponseFilterBuildItem createLoggingRequestFilter() {
//        return new ContainerResponseFilterBuildItem.Builder(LoggingRequestFilter.class.getName())
//                .setRegisterAsBean(true)
//                .setPriority(1)
//                .build();
//    }

    @BuildStep
    public AdditionalBeanBuildItem createUserPrincipalBean() {
        return new AdditionalBeanBuildItem(UserSecurityIdentityAugmentor.class);
    }
//
//    @BuildStep
//    public AdditionalBeanBuildItem createAdditionalBeanBuildItem() {
//        return new AdditionalBeanBuildItem(RegisterCustomizeModule.class);
//    }
}
