package io.yanmastra.keycloakuserservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.keycloak.representations.idm.RoleRepresentation;

public class RoleGroupDetailDetailDto extends RoleGroupDetailDto{
    @JsonProperty("role")
    public RoleRepresentation role;

    public RoleGroupDetailDetailDto() {
    }

    public RoleGroupDetailDetailDto(String id, String groupId, String roleId, RoleRepresentation role) {
        super(id, groupId, roleId);
        this.role = role;
    }
}
