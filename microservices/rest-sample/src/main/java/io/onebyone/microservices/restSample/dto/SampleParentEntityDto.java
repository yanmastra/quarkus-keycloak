package io.yanmastra.microservices.restSample.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.yanmastra.microservices.restSample.data.entity.SampleCategory;
import io.yanmastra.microservices.restSample.data.entity.SampleParentEntity;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SampleParentEntityDto {
    @JsonProperty("id")
    public String id;
    @JsonProperty("name")
    public String name;
    @JsonProperty("category")
    public SampleCategory category;
    @JsonProperty("price")
    public BigDecimal price = BigDecimal.ZERO;
    @JsonProperty("created_at")
    public ZonedDateTime createdAt;

    public SampleParentEntityDto() {
    }

    public static SampleParentEntityDto fromEntity(SampleParentEntity entity) {
        SampleParentEntityDto json = new SampleParentEntityDto();
        json.id = entity.getId();
        json.name = entity.getName();
        json.category = entity.getCategory();
        json.price = entity.getPrice();
        json.createdAt = entity.getCreatedAt();
        return json;
    }

    public SampleParentEntity toEntity() {
        SampleParentEntity entity = new SampleParentEntity();
        entity.setId(this.id);
        entity.setName(this.name);
        entity.setCategory(this.category);
        entity.setPrice(this.price);
        entity.setCreatedAt(this.createdAt);
        return entity;
    }
}
