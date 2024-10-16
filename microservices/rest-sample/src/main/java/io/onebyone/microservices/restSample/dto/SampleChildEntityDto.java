package io.onebyone.microservices.restSample.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.onebyone.microservices.restSample.data.entity.SampleChildEntity;
import io.onebyone.microservices.restSample.data.entity.SampleParentEntity;
import org.apache.commons.lang3.StringUtils;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SampleChildEntityDto {
    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("parent_id")
    private String parentId;
    @JsonProperty("parent_name")
    private String parentName;

    public SampleChildEntityDto() {
    }

    public static SampleChildEntityDto fromEntity(SampleChildEntity entity) {
        SampleChildEntityDto json = new SampleChildEntityDto();
        json.setId(entity.getId());
        json.setName(entity.getName());
        json.setDescription(entity.getDescription());

        if (entity.getParent() != null) {
            json.setParentId(entity.getParent().getId());
            json.setParentName(entity.getParent().getName());
        }
        return json;
    }

    public SampleChildEntity toEntity() {
        SampleChildEntity entity = new SampleChildEntity();
        entity.setId(getId());
        entity.setName(getName());
        entity.setDescription(getDescription());

        if (StringUtils.isNotBlank(getParentId())) {
            SampleParentEntity parentEntity = new SampleParentEntity();
            parentEntity.setId(getParentId());
            parentEntity.setName(getParentName());
            entity.setParent(parentEntity);
        }
        return entity;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }
}
