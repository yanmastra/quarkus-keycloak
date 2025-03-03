package io.onebyone.quarkus.microservices.common.deployment;

import io.onebyone.quarkus.microservices.common.crud.BasePaginationResource;
import io.onebyone.quarkus.microservices.common.crud.CrudableEndpointResource;
import io.onebyone.quarkus.microservices.common.crud.SelectablePaginationResource;
import io.onebyone.quarkus.microservices.common.repository.BaseRepository;
import io.onebyone.quarkus.microservices.common.utils.*;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.AdditionalIndexedClassesBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;

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
                CrudableEndpointResource.class.getName()
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
}
