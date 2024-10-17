package io.onebyone.quarkus.microservices.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.onebyone.quarkus.microservices.common.dto.BaseDto;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.util.Date;

/**
 * This class contains the basic columns that should be defined on every Entity
 */
@MappedSuperclass
public abstract class BaseEntity extends PanacheEntityBase implements Serializable {
    @CreationTimestamp(source = SourceType.DB)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;
    @Column(name = "created_by", length = 32)
    private String createdBy;
    @UpdateTimestamp(source = SourceType.DB)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;
    @Column(name = "updated_by", length = 32)
    private String updatedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "deleted_at")
    private Date deletedAt;
    @Column(name = "deleted_by", length = 32)
    private String deletedBy;

    public abstract String getId();
    public abstract void setId(String id);

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Date deletedAt) {
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
