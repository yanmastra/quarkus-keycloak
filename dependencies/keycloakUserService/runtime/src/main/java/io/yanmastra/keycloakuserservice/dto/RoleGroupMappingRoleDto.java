package io.yanmastra.keycloakuserservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public class RoleGroupMappingRoleDto extends RoleGroupDto implements Serializable {
    @JsonProperty("role_mapping")
    private List<SimpleAppFeatureDto> roleMapping;

    public List<SimpleAppFeatureDto> getRoleMapping() {
        return roleMapping;
    }

    public void setRoleMapping(List<SimpleAppFeatureDto> roleMapping) {
        this.roleMapping = roleMapping;
    }
}
