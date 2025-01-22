package io.yanmastra.keycloakuserservice.data;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.yanmastra.keycloakuserservice.data.entities.KeycloakRoleGroup;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.*;
import java.util.stream.Stream;

@ApplicationScoped
public class KcRoleGroupRepository implements PanacheRepositoryBase<KeycloakRoleGroup, String> {
    public Map<String, KeycloakRoleGroup> findGroupIn(Set<String> checkIsGroupExists) {
        Stream<KeycloakRoleGroup> result = find("where id in :ids", Map.of("ids", new ArrayList<>(checkIsGroupExists)))
                .stream();
        Map<String, KeycloakRoleGroup> roles = new HashMap<>();
        result.forEach(group -> roles.put(group.getId(), group));
        return roles;
    }

    public KeycloakRoleGroup findUserGroupById(String companyId, String groupId) {
        String parentPath = String.format("/%s/%s", "users", companyId);
        KeycloakRoleGroup parent = find("where path=?1", parentPath).firstResult();
        if(parent != null) {
            return find("where id=?1 and parent.id=?2", groupId, parent.getId()).firstResult();
        }
        return null;
    }

    public List<KeycloakRoleGroup> findGroupsIn(Collection<String> groupIds) {
        return find("where id in :ids", Map.of("ids", groupIds)).list();
    }
}
