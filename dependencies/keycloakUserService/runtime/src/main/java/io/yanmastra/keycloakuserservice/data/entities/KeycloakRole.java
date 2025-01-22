package io.yanmastra.keycloakuserservice.data.entities;

import io.yanmastra.quarkus.microservices.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.SQLDelete;

@Entity
@Table(name = "kc_role")
@SQLDelete(sql = "UPDATE kc_role SET deleted_at=NOW() WHERE id=?")
@Filter(name = "deletedRoleFilter", condition = "deleted_at is not null = :isDeleted")
@FilterDef(name = "deletedRoleFilter", parameters = @ParamDef(name = "isDeleted", type = Boolean.class))
public class KeycloakRole extends BaseEntity {
    @Id
    @Column(nullable = false, length = 36)
    private String id;

    @Column(name = "name", length = 36, unique = true)
    private String name;

    @Column(name = "description", length = 64)
    private String description;

    @Column(name = "container_id", length = 36)
    private String containerId;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }
}
