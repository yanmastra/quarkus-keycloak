package io.yanmastra.quarkus.microservices.common.it.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.yanmastra.quarkus.microservices.common.dto.BaseDto;
import io.yanmastra.quarkus.microservices.common.it.entity.SampleChildEntity;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SampleChildEntityJson implements BaseDto<SampleChildEntity> {

    @JsonProperty("id")
    public String id;
    @JsonProperty("name")
    public String name;
    @JsonProperty("parents")
    public List<SampleEntityJson> parents;

    public SampleChildEntityJson() {}
    public SampleChildEntityJson(String id, String name, List<SampleEntityJson> parents) {
        SampleChildEntity sce = new SampleChildEntity();
        sce.setId(id);
        sce.name = name;
        sce.parents = parents == null ? null : parents.stream().map(SampleEntityJson::toEntity).toList();
    }

    public static SampleChildEntityJson toJson(SampleChildEntity sampleChildEntity) {
        SampleChildEntityJson json = new SampleChildEntityJson();
        json.setId(sampleChildEntity.getId());
        json.name = sampleChildEntity.name;
        json.parents = sampleChildEntity.parents == null ? null : sampleChildEntity.parents.stream().map(p -> SampleEntityJson.toJson(p, false)).toList();
        return json;
    }

    public SampleChildEntity toEntity() {
        SampleChildEntity childEntity = new SampleChildEntity();
        childEntity.setId(id);
        return toEntity(childEntity);
    }

    public SampleChildEntity toEntity(SampleChildEntity childEntity) {
        childEntity.name = name;
        childEntity.parents = parents == null ? null : parents.stream().map(SampleEntityJson::toEntity).toList();
        return childEntity;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getCreatedBy() {
        return "";
    }

    @Override
    public void setCreatedBy(String createdBy) {

    }

    @Override
    public String getUpdatedBy() {
        return "";
    }

    @Override
    public void setUpdatedBy(String updatedBy) {

    }

    @Override
    public String getDeletedBy() {
        return "";
    }

    @Override
    public void setDeletedBy(String deletedBy) {

    }
}
