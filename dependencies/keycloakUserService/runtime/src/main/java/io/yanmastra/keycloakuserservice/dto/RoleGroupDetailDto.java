package io.yanmastra.keycloakuserservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleGroupDetailDto implements Serializable {
    @JsonProperty("id")
    public String id;
    @JsonProperty("group_id")
    public String groupId;
    @JsonProperty("role_id")
    public String roleId;

    public RoleGroupDetailDto() {
    }

    public RoleGroupDetailDto(String id, String groupId, String roleId) {
        this.id = id;
        this.groupId = groupId;
        this.roleId = roleId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }
}
