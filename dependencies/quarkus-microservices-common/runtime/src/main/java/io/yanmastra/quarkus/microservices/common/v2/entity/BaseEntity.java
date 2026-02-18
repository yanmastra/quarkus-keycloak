package io.yanmastra.quarkus.microservices.common.v2.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.yanmastra.quarkus.microservices.common.dto.BaseDto;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * This class contains the basic columns that should be defined on every Entity
 */
@MappedSuperclass
public abstract class BaseEntity<Id> extends PanacheEntityBase implements Serializable {
    @CreationTimestamp(source = SourceType.DB)
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
    @Column(name = "created_by", length = 32)
    private String createdBy;
    @UpdateTimestamp(source = SourceType.DB)
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
    @Column(name = "updated_by", length = 32)
    private String updatedBy;
    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
    @Column(name = "deleted_by", length = 32)
    private String deletedBy;

    public abstract Id getId();
    public abstract void setId(Id id);

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public OffsetDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(OffsetDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    /**
     * This method is deprecated, please use Helper class to convert Entity to DTO
     * @return null
     * @param
     */
    @Deprecated
    protected <Dto> Dto toDto(){
        return null;
    }

    /**
     * This method is deprecated, please use Helper class to convert Entity to DTO
     * @return
     * @param dto
     */
    @Deprecated
    public static <BE extends BaseEntity, Dto extends BaseDto<BE>> BE fromDto(Dto dto) {
        return dto.toEntity();
    }

    /**
     * This method is deprecated, please use Helper class to convert Entity to DTO
     * @param dto
     */
    @Deprecated
    public <Dto> void updateByDto(Dto dto){
    }
}
