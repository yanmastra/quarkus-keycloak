package io.yanmastra.microservices.common.it.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.yanmastra.microservices.common.it.entity.SampleEntity;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SampleEntityJson {
    @JsonProperty("id")
    public String id;
    @JsonProperty("name")
    public String name;
    @JsonProperty("category")
    public String category;
    @JsonProperty("price")
    public BigDecimal price = BigDecimal.ZERO;

    public SampleEntityJson() {
    }

    public static SampleEntityJson fromJson(SampleEntity entity) {
        SampleEntityJson json = new SampleEntityJson();
        json.id = entity.id;
        json.name = entity.name;
        json.category = entity.category;
        json.price = entity.price;
        return json;
    }

    public SampleEntity toJson() {
        SampleEntity entity = new SampleEntity();
        entity.id = this.id;
        entity.name = this.name;
        entity.category = this.category;
        entity.price = this.price;
        return entity;
    }
}
