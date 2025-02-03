package io.onebyone.quarkus.microservices.common.v2.dto;

import java.io.Serializable;

public interface BaseDto<Entity, Id> extends Serializable {
    @Deprecated
    default Entity toEntity() {
        return null;
    }


    Id getId();
    void setId(Id id);

    String getCreatedBy();

    void setCreatedBy(String createdBy);

    String getUpdatedBy();

    void setUpdatedBy(String updatedBy);

    String getDeletedBy();

    void setDeletedBy(String deletedBy);
}
