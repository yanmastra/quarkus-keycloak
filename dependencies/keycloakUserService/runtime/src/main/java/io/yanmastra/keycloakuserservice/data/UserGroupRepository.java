package io.yanmastra.keycloakuserservice.data;

import io.yanmastra.keycloakuserservice.data.entities.UserRoleGroup;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserGroupRepository implements PanacheRepositoryBase<UserRoleGroup, String> {
}
