package io.yanmastra.keycloakuserservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserRoleGroupDto {
    @JsonProperty("id")
    public String id;
    @JsonProperty("group")
    public RoleGroupDto group;

    public UserRoleGroupDto() {
    }
    public UserRoleGroupDto(String id, RoleGroupDto group) {
        this.id = id;
        this.group = group;
    }
}
