package io.yanmastra.authentication.deployment;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.resteasy.reactive.spi.ContainerResponseFilterBuildItem;
import io.quarkus.resteasy.reactive.spi.ExceptionMapperBuildItem;
import io.smallrye.jwt.auth.principal.JWTCallerPrincipalFactory;
import io.yanmastra.authentication.logging.LoggingRequestFilter;
import io.yanmastra.authentication.provider.ErrorMapper;
import io.yanmastra.authentication.provider.RegisterCustomizeModule;
import io.yanmastra.authentication.security.AuthenticationMechanism;
import io.yanmastra.authentication.security.AuthenticationService;
import io.yanmastra.authentication.security.BaseSecurityIdentityAugmentor;
import io.yanmastra.authentication.service.ThreadPoolExecutorService;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;

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

    private boolean isProvided(CombinedIndexBuildItem combinedIndexBuildItem, Class<?> clazz) {
        IndexView indexView = combinedIndexBuildItem.getIndex();
        DotName serviceName = DotName.createSimple(clazz);
        return !indexView.getAllKnownSubclasses(serviceName).isEmpty()
                || !indexView.getAllKnownImplementors(serviceName).isEmpty();
    }

    @BuildStep
    public void createAuthMechanismAnnotation(CombinedIndexBuildItem combinedIndexBuildItem, BuildProducer<AdditionalBeanBuildItem> beans) {
        boolean isNotProvided = !isProvided(combinedIndexBuildItem, AuthenticationMechanism.class);
        if (isNotProvided) {
            beans.produce(AdditionalBeanBuildItem.builder().addBeanClass(AuthenticationMechanism.class).setUnremovable().build());
        }
    }

    @BuildStep
    public ContainerResponseFilterBuildItem createLoggingResponseFilter() {
        return new ContainerResponseFilterBuildItem.Builder(LoggingRequestFilter.class.getName())
                .setRegisterAsBean(false)
                .setPriority(1)
                .build();
    }

    @BuildStep
    public void provideSecurityIdentityAugmentor(CombinedIndexBuildItem combinedIndexBuildItem, BuildProducer<AdditionalBeanBuildItem> beans) {
        if (!isProvided(combinedIndexBuildItem, BaseSecurityIdentityAugmentor.class)) {
            beans.produce(AdditionalBeanBuildItem.builder().addBeanClass(BaseSecurityIdentityAugmentor.class).setUnremovable().build());
        }
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

    @BuildStep
    public AdditionalBeanBuildItem createThreadPoolExecutorBean() {
        return AdditionalBeanBuildItem.unremovableOf(ThreadPoolExecutorService.class);
    }
}
