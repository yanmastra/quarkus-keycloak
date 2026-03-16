package io.yanmastra.authentication.deployment;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.resteasy.reactive.spi.ContainerResponseFilterBuildItem;
import io.quarkus.resteasy.reactive.spi.ExceptionMapperBuildItem;
import io.smallrye.jwt.auth.principal.JWTCallerPrincipalFactory;
import io.yanmastra.authentication.security.AuthenticationMechanism;
import io.yanmastra.authentication.security.AuthenticationService;
import io.yanmastra.authentication.security.BaseSecurityIdentityAugmentor;
import io.yanmastra.authentication.service.ThreadPoolExecutorService;
import io.yanmastra.quarkusBase.logging.LoggingRequestFilter;
import io.yanmastra.quarkusBase.provider.ErrorMapper;
import io.yanmastra.quarkusBase.provider.RegisterCustomizeModule;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;

class AuthenticationProcessor {

    private static final String FEATURE = "quarkus-authentication";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    public void createErrorMapper(CombinedIndexBuildItem combinedIndexBuildItem, BuildProducer<ExceptionMapperBuildItem> beans) {
        boolean isProvided = isProvided(combinedIndexBuildItem, ErrorMapper.class);
        if (!isProvided) {
            ExceptionMapperBuildItem bean = new ExceptionMapperBuildItem.Builder(ErrorMapper.class.getName(), Exception.class.getName())
                    .setRegisterAsBean(true)
                    .setPriority(1)
                    .build();
            beans.produce(bean);
        }
    }

    private boolean isProvided(CombinedIndexBuildItem combinedIndexBuildItem, Class<?> clazz) {
        IndexView indexView = combinedIndexBuildItem.getIndex();
        DotName serviceName = DotName.createSimple(clazz);
        return !indexView.getAllKnownSubclasses(serviceName).isEmpty()
                || !indexView.getAllKnownImplementations(serviceName).isEmpty();
    }

    @BuildStep
    public void createAuthMechanismAnnotation(CombinedIndexBuildItem combinedIndexBuildItem, BuildProducer<AdditionalBeanBuildItem> beans) {
        boolean isNotProvided = !isProvided(combinedIndexBuildItem, AuthenticationMechanism.class);
        if (isNotProvided) {
            beans.produce(AdditionalBeanBuildItem.builder().addBeanClass(AuthenticationMechanism.class).setUnremovable().build());
        }
    }

    @BuildStep
    public void createLoggingResponseFilter(CombinedIndexBuildItem combinedIndexBuildItem, BuildProducer<ContainerResponseFilterBuildItem> beans) {
        boolean isProvided = isProvided(combinedIndexBuildItem, LoggingRequestFilter.class);
        if (!isProvided) {
            ContainerResponseFilterBuildItem bean = new ContainerResponseFilterBuildItem.Builder(LoggingRequestFilter.class.getName())
                    .setRegisterAsBean(false)
                    .setPriority(1)
                    .build();
            beans.produce(bean);
        }
    }

    @BuildStep
    public void provideSecurityIdentityAugmentor(CombinedIndexBuildItem combinedIndexBuildItem, BuildProducer<AdditionalBeanBuildItem> beans) {
        if (!isProvided(combinedIndexBuildItem, BaseSecurityIdentityAugmentor.class)) {
            beans.produce(AdditionalBeanBuildItem.unremovableOf(BaseSecurityIdentityAugmentor.class));
        }
    }

    @BuildStep
    public void createAdditionalBeanBuildItem(CombinedIndexBuildItem combinedIndexBuildItem, BuildProducer<AdditionalBeanBuildItem> beans) {
        if (!isProvided(combinedIndexBuildItem, RegisterCustomizeModule.class)) {
            beans.produce(AdditionalBeanBuildItem.unremovableOf(RegisterCustomizeModule.class));
        }
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
    public void createThreadPoolExecutorBean(CombinedIndexBuildItem combinedIndexBuildItem, BuildProducer<AdditionalBeanBuildItem> beans) {
        if (!isProvided(combinedIndexBuildItem, ThreadPoolExecutorService.class)) {
            beans.produce(AdditionalBeanBuildItem.unremovableOf(ThreadPoolExecutorService.class));
        }
    }
}
