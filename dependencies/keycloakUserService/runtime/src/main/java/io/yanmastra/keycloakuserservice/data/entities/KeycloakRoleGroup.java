package io.yanmastra.keycloakuserservice.data.entities;

import io.yanmastra.quarkus.microservices.common.entity.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.SQLDelete;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "kc_role_group")
@SQLDelete(sql = "UPDATE kc_role_group SET deleted_at=NOW() WHERE id=?")
@Filter(name = "deletedGroupFilter", condition = "deleted_at is not null = :isDeleted")
@FilterDef(name = "deletedGroupFilter", parameters = @ParamDef(name = "isDeleted", type = Boolean.class))
public class KeycloakRoleGroup  extends BaseEntity {
    @Id
    @Column(nullable = false, length = 36)
    private String id;

    @Column(name = "name", length = 36, unique = true)
    private String name;

    @Column(name = "label", length = 36)
    private String label;

    @Column(name = "kc_grp_path", length = 128)
    private String path;

    @Column(name = "kc_sub_group_count")
    private Long subGroupCount = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    private KeycloakRoleGroup parent = null;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "group", orphanRemoval = true, cascade = CascadeType.MERGE)
    private List<KcGroupDetail> groupDetails = new ArrayList<>();

    @Transient
    private String parentId;

    public KeycloakRoleGroup() {
    }

    public KeycloakRoleGroup(String id) {
        this.id = id;
    }

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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getSubGroupCount() {
        return subGroupCount;
    }

    public void setSubGroupCount(Long subGroupCount) {
        this.subGroupCount = subGroupCount;
    }

    public KeycloakRoleGroup getParent() {
        return parent;
    }

    public void setParent(KeycloakRoleGroup parent) {
        this.parent = parent;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public List<KcGroupDetail> getGroupDetails() {
        return groupDetails;
    }

    public void setGroupDetails(List<KcGroupDetail> groupDetails) {
        this.groupDetails = groupDetails;
    }
}
