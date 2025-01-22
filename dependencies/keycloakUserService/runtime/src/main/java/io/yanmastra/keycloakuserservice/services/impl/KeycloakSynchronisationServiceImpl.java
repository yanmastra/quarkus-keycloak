package io.yanmastra.keycloakuserservice.services.impl;

import io.yanmastra.keycloakuserservice.Helper;
import io.yanmastra.keycloakuserservice.UserManagementConstant;
import io.yanmastra.keycloakuserservice.data.*;
import io.yanmastra.keycloakuserservice.data.entities.*;
import io.yanmastra.keycloakuserservice.dto.RealmMappingsRepresentation;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class KeycloakSynchronisationServiceImpl {
    @Inject
    KeycloakAdminRepository kcAdminRepository;
    @Inject
    KcRoleRepository kcRoleRepository;
    @Inject
    EntityManager entityManager;
    @Inject
    UsersRepository usersRepository;
    @Inject
    UserGroupRepository userGroupRepository;
    @Inject
    Helper helper;
    @Inject
    Logger log;
    @Inject
    KcRoleGroupRepository groupRepository;
    @Inject
    KcGroupDetailRepository groupDetailRepository;
    @ConfigProperty(name = "keycloak_realm")
    String realm;

    private KeycloakClient keycloakClient;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * Initializes the KeycloakClient after the configuration properties are
     * injected.
     */
    @PostConstruct
    public void init() {
        if (keycloakClient == null) {
            kcAdminRepository.init();
            keycloakClient = kcAdminRepository.getKeycloakClient();
        }
    }

    void onStart(@Observes StartupEvent ev) {
        kcAdminRepository.init();
        log.info("getting roles");

        executorService.submit(() -> {
            try {
                initRole();
            } catch (Exception e){
                log.error(e.getMessage(), e);
            }
        });
    }

    // todo -> need to fixing this method
    @Transactional
    void initRole() {
        // load all realm roles from keycloak
        List<RoleRepresentation> roles = kcAdminRepository.getRoles(null, null, null);
        Stream<KeycloakRole> keycloakRoles = roles.stream().map(role -> helper.convertToEntity(role));
        Set<String> checkIsRoleExists = keycloakRoles.map(KeycloakRole::getId).collect(Collectors.toSet());

        Map<String, KeycloakRole> existing = kcRoleRepository.findRolesIn(checkIsRoleExists);
        log.debug("found existing roles: " + existing.size());
        keycloakRoles = roles.stream().map(role -> helper.convertToEntity(role)).map(item -> {
            if (existing.containsKey(item.getId())) return existing.remove(item.getId());
            else return item;
        });
        kcRoleRepository.findAll().stream().forEach(role -> {
            if (!checkIsRoleExists.contains(role.getId())) {
                entityManager.createNativeQuery("delete from kc_group_detail where role_id=?1").setParameter(1, role.getId()).executeUpdate();
                entityManager.createNativeQuery("delete from kc_role where id=?1").setParameter(1, role.getId()).executeUpdate();
            }
        });
        keycloakRoles.forEach(item -> kcRoleRepository.persist(item));

        // loading all groups from keycloak
        List<GroupRepresentation> groups = loadGroup(null);

        Map<String, Set<String>> groupRealmMappings = new HashMap<>();
        Stream<KeycloakRoleGroup> keycloakGroups = getKeycloakRoleGroupStream(groups, groupRealmMappings);
        groupRepository.persist(keycloakGroups);
        log.debug("found existing roles: " + existing.size());

        Map<String, KcGroupDetail> kcGroupDetailMap = new HashMap<>();
        for (String key : groupRealmMappings.keySet()) {
            for (String role : groupRealmMappings.get(key)) {
                KcGroupDetail detail = new KcGroupDetail();
                detail.setGroup(groupRepository.findById(key));
                detail.setRole(kcRoleRepository.findById(role));

                log.debug("groupRealmMappings:"+detail.getGroup().getId()+":"+detail.getRole().getId());
                kcGroupDetailMap.put(detail.getGroup().getId()+":"+detail.getRole().getId(), detail);
            }
        }

        Set<KcGroupDetail> kcGroupDetails = kcGroupDetailMap.keySet().stream().map(key -> {
            KcGroupDetail kcGroupDetail = kcGroupDetailMap.get(key);
            KcGroupDetail detail = groupDetailRepository.findByGroupAndRole(kcGroupDetail.getGroup().getId(), kcGroupDetail.getRole().getId());
            if (detail != null) {
                return null;
            }

            log.debug("kcGroupDetails:"+kcGroupDetail.getRole().getId()+" | "+kcGroupDetail.getGroup().getId());
            return kcGroupDetail;
        }).filter(Objects::nonNull).collect(Collectors.toSet());
        groupDetailRepository.persist(kcGroupDetails);


        List<UserRepresentation> users = kcAdminRepository.getAllUsers();
        if (!users.isEmpty()) {
            Set<User> userEntities = users.stream()
                    .map(user -> helper.convertToEntity(user))
                    .collect(Collectors.toSet());


            userEntities.forEach(user -> {
                boolean exists = usersRepository.count("where id=?1", user.getId()) > 0;
                if (!exists) {
                    usersRepository.persist(user);
                }

                List<GroupRepresentation> userGroup = kcAdminRepository.getUserGroup(user.getId());
                if (userGroup != null) {
                    userGroup.forEach(userGroupItem -> {
                        try {
                            boolean groupExists = userGroupRepository.count(
                                    "where user.id=:userId and group.id=:groupId",
                                    Map.of("userId", user.getId(), "groupId", userGroupItem.getId())
                            ) > 0;

                            if (!groupExists) {
                                UserRoleGroup urg = new UserRoleGroup(
                                        user,
                                        groupRepository.findById(userGroupItem.getId())
                                );
                                userGroupRepository.persist(urg);
                            }
                        } catch (Exception e){
                            log.debug(e.getMessage(), e);
                        }
                    });
                }
            });
        }

        log.info("loading all keycloak data: SUCCESS");
    }

    private Stream<KeycloakRoleGroup> getKeycloakRoleGroupStream(List<GroupRepresentation> groups, Map<String, Set<String>> groupRealmMappings) {
        Map<String, KeycloakRoleGroup> groupRoleGroupsMap = new HashMap<>();
        Stream<KeycloakRoleGroup> keycloakGroups = groups.stream().map(group -> {
            if (!groupRealmMappings.containsKey(group.getId())) groupRealmMappings.put(group.getId(), new HashSet<>());

            // load realm mapping from keycloak
            RealmMappingsRepresentation realmMappings = getRealmMappings(group.getId());
            groupRealmMappings.get(group.getId()).addAll(realmMappings.realmMappings == null ? Set.of() : realmMappings.realmMappings.stream().map(RoleRepresentation::getId).toList());
            KeycloakRoleGroup entity = helper.convertToEntity(group);
            groupRoleGroupsMap.put(group.getId(), entity);
            return entity;
        });

        keycloakGroups = keycloakGroups.map(group -> {
            if (StringUtils.isNotBlank(group.getParentId()) && groupRoleGroupsMap.containsKey(group.getParentId())) {
                group.setParent(groupRoleGroupsMap.get(group.getParentId()));
            }
            KeycloakRoleGroup kcRoleGroup = groupRepository.findById(group.getId());
            if (kcRoleGroup != null) {
                group = kcRoleGroup;
            }
            return group;
        });
        return keycloakGroups;
    }


    public RealmMappingsRepresentation getRealmMappings(String id) {
        RealmMappingsRepresentation response = keycloakClient.getRealmMappings(
                UserManagementConstant.BEARER_TOKEN + kcAdminRepository.getAdminAccessToken(),
                realm,
                id
        );
        log.debug("get realm mappings: " + response.realmMappings);
        return response;
    }


    private List<GroupRepresentation> loadGroup(String parentId) {
        List<GroupRepresentation> groups = StringUtils.isBlank(parentId) ?
                kcAdminRepository.getRoleGroups(null, null, null):
                kcAdminRepository.getRoleGroupChildren(parentId, null, null, null);

        List<GroupRepresentation> result = new ArrayList<>(groups);
        for (GroupRepresentation group : groups) {
            if (group.getSubGroupCount() > 0) {
                List<GroupRepresentation> children = loadGroup(group.getId());
                result.addAll(children);
            }
        }
        return result;
    }

}
