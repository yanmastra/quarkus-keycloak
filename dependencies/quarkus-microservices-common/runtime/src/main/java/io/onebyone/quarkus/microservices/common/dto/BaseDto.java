package io.onebyone.quarkus.microservices.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface BaseDto<Entity> extends io.onebyone.quarkus.microservices.common.v2.dto.BaseDto<Entity, String> {
    @Deprecated
    default Entity toEntity() {
        return null;
    }


    String getId();
    void setId(String id);

    @JsonIgnore
    String getCreatedBy();

    void setCreatedBy(String createdBy);

    @JsonIgnore
    String getUpdatedBy();

    void setUpdatedBy(String updatedBy);

    @JsonIgnore
    String getDeletedBy();

    void setDeletedBy(String deletedBy);
}
