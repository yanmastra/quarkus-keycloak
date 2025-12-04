package io.yanmastra.quarkusBase.quarkusBase.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.security.Principal;
import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIncludeProperties(value = {"id", "username", "name", "email", "profile_name", "current_tenant", "tenant_access", "authorities"})
public interface UserPrincipal extends Principal, Serializable {

    @JsonProperty("id")
    String getUserId();

    @JsonProperty("username")
    String getUsername();

    String getName();
    @JsonProperty("email")
    String getEmail();
    @JsonProperty("profile_name")
    String getProfileName();
    @JsonProperty("current_tenant")
    String getCurrentTenant();

    @JsonProperty("authorities")
    Set<String> getAuthorities();

    @JsonProperty("tenant_access")
    Set<String> tenantAccess();

    @JsonIgnore
    String getSessionState();

    Object getAdditionalClaim(String key);
}
