package io.onebyone.keycloakuserservice.deployment;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

class KeycloakUserServiceProcessor {

    private static final String FEATURE = "keycloakUserService";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }
}
