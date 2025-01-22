package io.yanmastra.keycloakuserservice.data;

import io.yanmastra.keycloakuserservice.data.entities.User;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UsersRepository implements PanacheRepositoryBase<User, String> {
}
