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

    public SampleChildEntityJson() {}
    public SampleChildEntityJson(Long id, String name, List<SampleEntityJson> parents) {}

    public static SampleChildEntityJson toJson(SampleChildEntity sampleChildEntity) {
        SampleChildEntityJson json = new SampleChildEntityJson();
        json.setId(sampleChildEntity.getId());
        json.name = sampleChildEntity.name;
        return json;
    }

    public SampleChildEntity toEntity() {
        SampleChildEntity childEntity = new SampleChildEntity();
        childEntity.setId(id);
        return toEntity(childEntity);
    }

    public SampleChildEntity toEntity(SampleChildEntity childEntity) {
        childEntity.name = name;
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
