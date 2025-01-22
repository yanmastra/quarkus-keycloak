package io.yanmastra.keycloakuserservice.services;

import io.yanmastra.keycloakuserservice.data.entities.KeycloakRoleGroup;
import io.yanmastra.keycloakuserservice.dto.RoleGroupMappingRoleDto;

public interface GroupService {
    void fetchGroupMapping(RoleGroupMappingRoleDto group, KeycloakRoleGroup entity);
    void fetchGroupMapping(RoleGroupMappingRoleDto group);
}
