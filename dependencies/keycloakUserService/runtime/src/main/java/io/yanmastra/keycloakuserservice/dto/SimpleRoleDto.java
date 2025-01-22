package io.yanmastra.keycloakuserservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SimpleRoleDto implements Serializable {
    public String id;
    public String name;
    public String description;
    @JsonProperty("is_allowed")
    public Boolean isAllowed = false;
}
