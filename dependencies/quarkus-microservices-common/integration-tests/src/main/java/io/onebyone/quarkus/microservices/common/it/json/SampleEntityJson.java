package io.onebyone.quarkus.microservices.common.it.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.onebyone.quarkus.microservices.common.dto.BaseDto;
import io.onebyone.quarkus.microservices.common.it.entity.SampleEntity;
import io.onebyone.quarkus.microservices.common.it.entity.SampleType;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SampleEntityJson implements BaseDto<SampleEntity> {
    @JsonProperty("id")
    public String id;
    @JsonProperty("name")
    public String name;
    @JsonProperty("category")
    public String category;
    @JsonProperty("price")
    public BigDecimal price = BigDecimal.ZERO;
    @JsonProperty("is_active")
    public boolean isActive;
    @JsonProperty("sample_type")
    public SampleType sampleType;

    public SampleEntityJson() {
    }

    public static SampleEntityJson fromJson(SampleEntity entity) {
        SampleEntityJson json = new SampleEntityJson();
        json.id = entity.id;
        json.name = entity.name;
        json.category = entity.category;
        json.price = entity.price;
        json.isActive = entity.isActive;
        json.sampleType = entity.sampleType;
        return json;
    }

    public SampleEntity toJson() {
        SampleEntity entity = new SampleEntity();
        entity.id = this.id;
        entity.name = this.name;
        entity.category = this.category;
        entity.price = this.price;
        entity.isActive = this.isActive;
        entity.sampleType = this.sampleType;
        return entity;
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
