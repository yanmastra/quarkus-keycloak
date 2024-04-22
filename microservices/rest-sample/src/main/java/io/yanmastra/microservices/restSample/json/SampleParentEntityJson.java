package io.yanmastra.microservices.restSample.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.yanmastra.microservices.restSample.entity.SampleCategory;
import io.yanmastra.microservices.restSample.entity.SampleParentEntity;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SampleParentEntityJson {
    @JsonProperty("id")
    public String id;
    @JsonProperty("name")
    public String name;
    @JsonProperty("category")
    public SampleCategory category;
    @JsonProperty("price")
    public BigDecimal price = BigDecimal.ZERO;

    public SampleParentEntityJson() {
    }

    public static SampleParentEntityJson fromEntity(SampleParentEntity entity) {
        SampleParentEntityJson json = new SampleParentEntityJson();
        json.id = entity.getId();
        json.name = entity.getName();
        json.category = entity.getCategory();
        json.price = entity.getPrice();
        return json;
    }

    public SampleParentEntity toEntity() {
        SampleParentEntity entity = new SampleParentEntity();
        entity.setId(this.id);
        entity.setName(this.name);
        entity.setCategory(this.category);
        entity.setPrice(this.price);
        return entity;
    }
}
