package io.yanmastra.quarkus.microservices.common.v2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

public abstract class BaseDtoImpl<Entity, Id> implements BaseDto<Entity, Id> {
    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
    @JsonProperty("created_by")
    private String createdBy;
    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;
    @JsonProperty("updated_by")
    private String updatedBy;
    @JsonProperty("deleted_at")
    private OffsetDateTime deletedAt;
    @JsonProperty("deleted_by")
    private String deletedBy;

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt == null ? null : createdAt.toOffsetDateTime();
    }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt == null ? null : updatedAt.toOffsetDateTime();
    }

    @Override
    public String getUpdatedBy() {
        return updatedBy;
    }

    @Override
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public OffsetDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(OffsetDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    @Override
    public String getDeletedBy() {
        return deletedBy;
    }

    @Override
    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }
}
