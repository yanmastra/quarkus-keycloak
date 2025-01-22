package io.yanmastra.keycloakuserservice.data;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.yanmastra.keycloakuserservice.data.entities.KeycloakRole;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@ApplicationScoped
public class KcRoleRepository implements PanacheRepositoryBase<KeycloakRole, String>  {
    public Map<String, KeycloakRole> findRolesIn(Set<String> checkExistingRole) {
        Stream<KeycloakRole> result = find("where id in :ids", Map.of("ids", new ArrayList<>(checkExistingRole)))
                .stream();
        Map<String, KeycloakRole> roles = new HashMap<>();
        result.forEach(role -> roles.put(role.getId(), role));
        return roles;
    }
}
