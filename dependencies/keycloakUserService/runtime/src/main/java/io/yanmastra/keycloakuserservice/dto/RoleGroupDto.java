package io.yanmastra.keycloakuserservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleGroupDto implements Serializable {
    @JsonProperty("id")
    public String id;
    @JsonProperty("name")
    public String name;
    @JsonProperty("label")
    public String label;
    @JsonProperty("kc_grp_path")
    public String path;
    @JsonProperty("kc_sub_group_count")
    public Long subGroupCount = 0L;
    @JsonProperty("parent_id")
    public String parentId;
    @JsonProperty("details")
    public List<RoleGroupDetailDto> details;

    public RoleGroupDto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getSubGroupCount() {
        return subGroupCount;
    }

    public void setSubGroupCount(Long subGroupCount) {
        this.subGroupCount = subGroupCount;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public List<RoleGroupDetailDto> getDetails() {
        return details;
    }

    public void setDetails(List<? extends RoleGroupDetailDto> details) {
        if (this.details == null) this.details = new ArrayList<>();
        else this.details.clear();

        this.details.addAll(details);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
