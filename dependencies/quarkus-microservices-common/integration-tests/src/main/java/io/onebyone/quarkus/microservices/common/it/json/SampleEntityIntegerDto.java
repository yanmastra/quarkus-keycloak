package io.onebyone.quarkus.microservices.common.it.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.onebyone.quarkus.microservices.common.it.entity.SampleEntityInteger;
import io.onebyone.quarkus.microservices.common.v2.dto.BaseDto;

public class SampleEntityIntegerDto implements BaseDto<SampleEntityInteger, Long> {

    @JsonProperty("id")
    private Long id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;
    @JsonProperty("created_by")
    private String createdBy;


    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long aLong) {
        this.id = aLong;
    }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public String getUpdatedBy() {
        return null;
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
}
