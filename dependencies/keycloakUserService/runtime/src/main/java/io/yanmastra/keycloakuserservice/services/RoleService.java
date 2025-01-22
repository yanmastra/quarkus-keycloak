package io.yanmastra.keycloakuserservice.services;

import io.yanmastra.keycloakuserservice.data.entities.KeycloakRoleGroup;
import io.yanmastra.keycloakuserservice.dto.RoleGroupDto;
import jakarta.ws.rs.core.SecurityContext;

public interface RoleService {
    String getIdByPath(String path);

    KeycloakRoleGroup updateRoleGroup(KeycloakRoleGroup entity, RoleGroupDto dto);

    RoleGroupDto fetchDetails(RoleGroupDto dto);

    RoleGroupDto createGroup(RoleGroupDto dao, SecurityContext context);
}
