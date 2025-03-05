package io.onebyone.quarkus.microservices.common.it.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.onebyone.quarkus.microservices.common.dto.BaseDto;
import io.onebyone.quarkus.microservices.common.it.entity.SampleChildEntity;
import io.onebyone.quarkus.microservices.common.it.entity.SampleEntity;
import io.onebyone.quarkus.microservices.common.it.entity.SampleType;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    @JsonProperty("x_date")
    public Date date;
    @JsonProperty("x_date_time")
    public ZonedDateTime dateTime;
    @JsonProperty("children")
    public List<SampleChildEntityJson> children;

    public SampleEntityJson() {
    }

    public static SampleEntityJson toJson(SampleEntity entity) {
        SampleEntityJson json = new SampleEntityJson();
        json.id = entity.id;
        json.name = entity.name;
        json.category = entity.category;
        json.price = entity.price;
        json.isActive = entity.isActive;
        json.sampleType = entity.sampleType;
        json.date = entity.date;
        json.dateTime = entity.dateTime;
        if (entity.children != null) {
            json.children = entity.children.stream().map(SampleChildEntityJson::toJson).toList();
        }
        return json;
    }

    public SampleEntity toEntity() {
        SampleEntity entity = new SampleEntity();
        entity.id = this.id;
        entity.name = this.name;
        entity.category = this.category;
        entity.price = this.price;
        entity.isActive = this.isActive;
        entity.sampleType = this.sampleType;
        entity.date = this.date;
        entity.dateTime = this.dateTime;
        if (children != null) {
            entity.children = children.stream().map(SampleChildEntityJson::toEntity).toList();
        } else  {
            entity.children = null;
        }
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
