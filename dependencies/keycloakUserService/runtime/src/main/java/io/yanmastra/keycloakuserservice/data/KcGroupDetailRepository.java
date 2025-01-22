package io.yanmastra.keycloakuserservice.data;

import io.yanmastra.keycloakuserservice.data.entities.KcGroupDetail;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class KcGroupDetailRepository implements PanacheRepositoryBase<KcGroupDetail, String> {
    public List<KcGroupDetail> findByGroup(String groupId) {
        return find("where group.id=?1", groupId)
                .list();
    }

    public KcGroupDetail findByGroupAndRole(String groupId, String roleId) {
        return find("group.id=?1 and role.id=?2", groupId, roleId).firstResult();
    }
}
