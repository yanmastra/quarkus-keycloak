package io.yanmastra.microservices.restSample.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.yanmastra.quarkus.microservices.common.dto.BaseDto;

import java.time.ZonedDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MyBaseDto<Entity> implements BaseDto<Entity> {
    private String id;
    @JsonProperty("created_at")
    private ZonedDateTime createdAt;
    @JsonProperty("updated_at")
    private ZonedDateTime updatedAt;
    @JsonProperty("deleted_at")
    private ZonedDateTime deletedAt;
    @JsonProperty("created_by")
    private String createdBy;
    @JsonProperty("updated_by")
    private String updatedBy;
    @JsonProperty("deleted_by")
    private String deletedBy;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public ZonedDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(ZonedDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public String getUpdatedBy() {
        return updatedBy;
    }

    @Override
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
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
