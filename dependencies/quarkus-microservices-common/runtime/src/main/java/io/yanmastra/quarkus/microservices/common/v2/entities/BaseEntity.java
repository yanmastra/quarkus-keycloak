package io.yanmastra.quarkus.microservices.common.v2.entities;

import io.yanmastra.quarkus.microservices.common.v2.dto.BaseDto;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * This class contains the basic columns that should be defined on every Entity
 */
@MappedSuperclass
public abstract class BaseEntity<Id> extends PanacheEntityBase implements Serializable {
    @CreationTimestamp(source = SourceType.DB)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private ZonedDateTime createdAt;
    @Column(name = "created_by", length = 32)
    private String createdBy;
    @UpdateTimestamp(source = SourceType.DB)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;
    @Column(name = "updated_by", length = 32)
    private String updatedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "deleted_at")
    private ZonedDateTime deletedAt;
    @Column(name = "deleted_by", length = 32)
    private String deletedBy;

    public abstract Id getId();
    public abstract void setId(Id id);

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public ZonedDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(ZonedDateTime deletedAt) {
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
    public static <BE extends BaseEntity<Id>, Dto extends BaseDto<BE, Id>, Id> BE fromDto(Dto dto) {
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
