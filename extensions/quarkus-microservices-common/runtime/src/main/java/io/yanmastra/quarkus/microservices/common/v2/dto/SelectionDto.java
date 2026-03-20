package io.yanmastra.quarkus.microservices.common.v2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class SelectionDto {
    @JsonProperty("id")
    public String id;
    @JsonProperty("name")
    public String name;

    public SelectionDto() {
    }

    public SelectionDto(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
