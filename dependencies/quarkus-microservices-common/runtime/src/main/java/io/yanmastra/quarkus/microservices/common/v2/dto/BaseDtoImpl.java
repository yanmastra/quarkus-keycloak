package io.yanmastra.quarkus.microservices.common.v2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;

public abstract class BaseDtoImpl<Entity, Id> implements BaseDto<Entity, Id> {
    @JsonProperty("created_at")
    private ZonedDateTime createdAt;
    @JsonProperty("created_by")
    private String createdBy;
    @JsonProperty("updated_at")
    private ZonedDateTime updatedAt;
    @JsonProperty("updated_by")
    private String updatedBy;
    @JsonProperty("deleted_at")
    private ZonedDateTime deletedAt;
    @JsonProperty("deleted_by")
    private String deletedBy;

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String getUpdatedBy() {
        return updatedBy;
    }

    @Override
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public ZonedDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(ZonedDateTime deletedAt) {
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
