package io.yanmastra.keycloakuserservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SimpleAppFeatureDto implements Serializable {
    public Long id;
    public String label;
    @JsonProperty("realm_name")
    public String realmName;
    @JsonProperty("feature_key")
    public String featureKey;
    public List<SimpleRoleDto> accesses;

    public SimpleAppFeatureDto() {
    }

    public SimpleAppFeatureDto(Long id, String label, String realmName, String featureKey, List<SimpleRoleDto> accesses) {
        this.id = id;
        this.label = label;
        this.realmName = realmName;
        this.featureKey = featureKey;
        this.accesses = accesses;
    }
}
