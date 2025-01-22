package io.yanmastra.keycloakuserservice.data.entities;

import io.yanmastra.quarkus.microservices.common.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "kc_group_detail", indexes = {
        @Index(name = "_kc_group_detail_fk_unique", columnList = "group_id, role_id", unique = true)
})
public class KcGroupDetail extends BaseEntity {
    @Id
    @Column(length = 36, nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", referencedColumnName = "id", nullable = false)
    private KeycloakRoleGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", referencedColumnName = "id", nullable = false)
    private KeycloakRole role;

    public KcGroupDetail() {
    }

    public KcGroupDetail(KeycloakRoleGroup group, KeycloakRole role) {
        this.group = group;
        this.role = role;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public KeycloakRoleGroup getGroup() {
        return group;
    }

    public void setGroup(KeycloakRoleGroup group) {
        this.group = group;
    }

    public KeycloakRole getRole() {
        return role;
    }

    public void setRole(KeycloakRole role) {
        this.role = role;
    }
}
