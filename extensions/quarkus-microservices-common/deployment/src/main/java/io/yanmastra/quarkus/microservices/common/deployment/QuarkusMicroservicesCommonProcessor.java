package io.yanmastra.quarkus.microservices.common.deployment;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.AdditionalIndexedClassesBuildItem;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.resteasy.reactive.spi.ExceptionMapperBuildItem;
import io.yanmastra.quarkus.microservices.common.crud.BasePaginationResource;
import io.yanmastra.quarkus.microservices.common.crud.CrudableEndpointResource;
import io.yanmastra.quarkus.microservices.common.crud.SelectablePaginationResource;
import io.yanmastra.quarkus.microservices.common.repository.BaseRepository;
import io.yanmastra.quarkus.microservices.common.utils.*;
import io.yanmastra.quarkusBase.provider.ErrorMapper;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Singleton;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.ext.Provider;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;

import java.lang.annotation.Annotation;
import java.util.List;

class QuarkusMicroservicesCommonProcessor {

    private static final String FEATURE = "quarkus-microservices-common";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }


    @BuildStep
    AdditionalIndexedClassesBuildItem createIndexedClasses() {
        return new AdditionalIndexedClassesBuildItem(
                BaseRepository.class.getName(),
                BasePaginationResource.class.getName(),
                SelectablePaginationResource.class.getName(),
                CrudableEndpointResource.class.getName(),
                io.yanmastra.quarkus.microservices.common.v2.crud.BasePaginationResource.class.getName(),
                io.yanmastra.quarkus.microservices.common.v2.crud.CrudableEndpointResource.class.getName(),
                io.yanmastra.quarkus.microservices.common.v2.crud.SelectablePaginationResource.class.getName()
        );
    }

    @BuildStep
    List<AdditionalBeanBuildItem> createPramToQueryHandlerBean() {
        return List.of(
                AdditionalBeanBuildItem.unremovableOf(ParamToQueryEquals.class),
                AdditionalBeanBuildItem.unremovableOf(ParamToQueryIn.class),
                AdditionalBeanBuildItem.unremovableOf(ParamToQueryGreaterThan.class),
                AdditionalBeanBuildItem.unremovableOf(ParamToQueryLessThan.class),
                AdditionalBeanBuildItem.unremovableOf(ParamToQueryNotEquals.class),
                AdditionalBeanBuildItem.unremovableOf(ParamToQueryNotIn.class),
                AdditionalBeanBuildItem.unremovableOf(ParamToQueryRange.class),
                AdditionalBeanBuildItem.unremovableOf(ParamToQueryIsNull.class),
                AdditionalBeanBuildItem.unremovableOf(ParamToQueryIsNotNull.class),
                AdditionalBeanBuildItem.unremovableOf(ParamToQueryContains.class)
        );
    }

    @BuildStep
    void provideValueSeparator(CombinedIndexBuildItem combinedIndexBuildItem, BuildProducer<AdditionalBeanBuildItem> beans) {
        if (!isProvided(combinedIndexBuildItem, QueryParamParser.class, List.of(Singleton.class, ApplicationPath.class, RequestScoped.class))) {
            beans.produce(
                    AdditionalBeanBuildItem
                            .builder()
                            .addBeanClass(QueryParamParser.class)
                            .setUnremovable()
                            .build()
            );
        }
    }

    @BuildStep
    public void createErrorMapper(CombinedIndexBuildItem combinedIndexBuildItem, BuildProducer<ExceptionMapperBuildItem> beans) {
        boolean isProvided = isProvided(combinedIndexBuildItem, ErrorMapper.class, List.of(Provider.class, Singleton.class, ApplicationPath.class, RequestScoped.class));
        if (!isProvided) {
            ExceptionMapperBuildItem bean = new ExceptionMapperBuildItem.Builder(ErrorMapper.class.getName(), Exception.class.getName())
                    .setRegisterAsBean(true)
                    .setPriority(1)
                    .build();
            beans.produce(bean);
        }
    }

    private boolean isProvided(CombinedIndexBuildItem combinedIndexBuildItem, Class<?> eClass, List<Class<? extends Annotation>> hasAnnotations) {
        IndexView indexView = combinedIndexBuildItem.getIndex();
        DotName serviceName = DotName.createSimple(eClass);
        return (indexView.getAllKnownSubclasses(serviceName).stream()
                .anyMatch(clazz -> hasAnnotations.stream().anyMatch(clazz::hasAnnotation))
                )
                || !indexView.getAllKnownImplementations(serviceName).isEmpty();
    }
}
