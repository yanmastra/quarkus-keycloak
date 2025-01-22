package io.yanmastra.keycloakuserservice.deployment;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.AdditionalIndexedClassesBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.yanmastra.keycloakuserservice.data.*;
import io.yanmastra.keycloakuserservice.resource.AuthenticationResource;
import io.yanmastra.keycloakuserservice.resource.GroupResource;
import io.yanmastra.keycloakuserservice.resource.RoleResource;
import io.yanmastra.keycloakuserservice.resource.UserResource;
import io.yanmastra.keycloakuserservice.services.GroupService;
import io.yanmastra.keycloakuserservice.services.KeycloakAuthenticationService;
import io.yanmastra.keycloakuserservice.services.RoleService;
import io.yanmastra.keycloakuserservice.services.UserService;

class KeycloakUserServiceProcessor {

    private static final String FEATURE = "keycloakUserService";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    //todo
    @BuildStep
    AdditionalBeanBuildItem provideRepositoryBeans() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(KcRoleGroupRepository.class)
                .addBeanClass(KcRoleRepository.class)
                .addBeanClass(KcGroupDetailRepository.class)
                .addBeanClass(UserGroupRepository.class)
                .addBeanClass(UsersRepository.class)
                .build();
    }

    @BuildStep
    AdditionalBeanBuildItem provideServiceBeans() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(GroupService.class)
                .addBeanClass(KeycloakAuthenticationService.class)
                .addBeanClass(RoleService.class)
                .addBeanClass(UserService.class)
                .addBeanClass(KeycloakAdminRepository.class)
                .build();
    }

    @BuildStep
    AdditionalIndexedClassesBuildItem provideResources() {
        return new AdditionalIndexedClassesBuildItem(
                AuthenticationResource.class.getName(),
                GroupResource.class.getName(),
                UserResource.class.getName(),
                RoleResource.class.getName()
        );
    }


}
