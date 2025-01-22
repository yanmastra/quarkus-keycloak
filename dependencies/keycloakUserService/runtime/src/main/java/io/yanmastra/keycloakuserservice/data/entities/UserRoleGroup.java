package io.yanmastra.keycloakuserservice.data.entities;


import io.yanmastra.quarkus.microservices.common.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "rel_user_group", indexes = @Index(name = "_rel_user_group_fk_unique", columnList = "user_id, group_id", unique = true))
public class UserRoleGroup extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", referencedColumnName = "id", nullable = false)
    private KeycloakRoleGroup group;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public UserRoleGroup() {
    }

    public UserRoleGroup(User user, KeycloakRoleGroup group) {
        this.user = user;
        this.group = group;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public KeycloakRoleGroup getGroup() {
        return group;
    }

    public void setGroup(KeycloakRoleGroup group) {
        this.group = group;
    }
}
