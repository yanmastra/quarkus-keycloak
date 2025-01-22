package io.yanmastra.quarkus.microservices.common.dto;

import java.io.Serializable;

public interface BaseDto<Entity> extends Serializable {
    @Deprecated
    default Entity toEntity() {
        return null;
    }


    String getId();
    void setId(String id);

    String getCreatedBy();

    void setCreatedBy(String createdBy);

    String getUpdatedBy();

    void setUpdatedBy(String updatedBy);

    String getDeletedBy();

    void setDeletedBy(String deletedBy);
}
