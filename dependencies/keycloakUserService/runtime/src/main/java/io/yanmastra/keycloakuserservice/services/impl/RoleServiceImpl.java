package io.yanmastra.keycloakuserservice.services.impl;

import io.yanmastra.keycloakuserservice.Helper;
import io.yanmastra.keycloakuserservice.UserManagementConstant;
import io.yanmastra.keycloakuserservice.data.*;
import io.yanmastra.keycloakuserservice.data.entities.*;
import io.yanmastra.keycloakuserservice.dto.RoleGroupDetailDetailDto;
import io.yanmastra.keycloakuserservice.dto.RoleGroupDetailDto;
import io.yanmastra.keycloakuserservice.dto.RoleGroupDto;
import io.yanmastra.keycloakuserservice.services.RoleService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.keycloak.representations.idm.GroupRepresentation;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class RoleServiceImpl implements RoleService {
    @Inject
    KeycloakAdminRepository kcAdminRepository;
    @Inject
    KcRoleRepository kcRoleRepository;
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

    public String getIdByPath(String path) {
        KeycloakRoleGroup group = groupRepository.find("where path=:path", Map.of("path", path)).firstResult();
        if (group == null || group.getParent() == null) return null;
        return group.getId();
    }

    @Override
    public KeycloakRoleGroup updateRoleGroup(KeycloakRoleGroup entity, RoleGroupDto dto) {
        entity.setLabel(dto.label);

        if (dto.getDetails() != null) {
            Map<String, KcGroupDetail> updated = new HashMap<>();
            Set<String> newAssignedRole = new HashSet<>();

            List<KcGroupDetail> currentDetails = groupDetailRepository.findByGroup(entity.getId());
            // checking new group details, is there any unassigned roles and new assigned roles
            dto.getDetails().stream().map(item -> {
                        KcGroupDetail match = currentDetails.stream()
                                .filter(currentDetail -> currentDetail.getRole().getId().equals(item.roleId))
                                .findFirst().orElse(null);

                        // existing role
                        if (match != null) return match;

                        // new role assigned
                        newAssignedRole.add(item.roleId);
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .forEach(item -> updated.put(item.getRole().getId(), item));

            // collect unassigned roles
            Set<String> deleted = currentDetails.stream().filter(item -> !updated.containsKey(item.getRole().getId()))
                    .map(KcGroupDetail::getId).collect(Collectors.toSet());

            List<KcGroupDetail> newDetails = kcRoleRepository.findRolesIn(newAssignedRole)
                    .values().stream().map(role -> {
                        KcGroupDetail newDetail = new KcGroupDetail();
                        newDetail.setRole(role);
                        newDetail.setGroup(entity);
                        return newDetail;
                    }).toList();

            List<String> updatedRealmRoles = new ArrayList<>(updated.keySet().stream()
                    .map(key -> updated.get(key).getRole().getName())
                    .toList());
            updatedRealmRoles.addAll(newAssignedRole);

            GroupRepresentation groupRepresentation = new GroupRepresentation();
            groupRepresentation.setId(entity.getId());
            groupRepresentation.setName(dto.getName());
            groupRepresentation.setParentId(dto.getParentId());
            groupRepresentation.setRealmRoles(updatedRealmRoles);
            groupRepresentation.setPath(dto.path);
            kcAdminRepository.updateGroup(groupRepresentation);

            deleted.forEach(id -> groupDetailRepository.deleteById(id));
            groupDetailRepository.persist(newDetails.stream());
        }
        return entity;
    }

    @Override
    public RoleGroupDto fetchDetails(RoleGroupDto dto) {
        List<RoleGroupDetailDetailDto> detailDtoList = groupDetailRepository.findByGroup(dto.id)
                .stream().map(item -> new RoleGroupDetailDetailDto(
                        item.getId(), item.getGroup().getId(),
                        item.getRole().getId(),
                        helper.convertToDto(item.getRole())
                )).toList();
        dto.setDetails(detailDtoList);
        return dto;
    }

    @Transactional
    @Override
    public RoleGroupDto createGroup(RoleGroupDto dao, SecurityContext context) {
        GroupRepresentation result = kcAdminRepository.createGroup(dao.getParentId(), dao.getName());
        dao.setId(result.getId());
        dao.setPath(result.getPath());
        dao.setSubGroupCount(result.getSubGroupCount());

        KeycloakRoleGroup group = helper.convertToEntity(dao);
        groupRepository.persist(group);

        Map<String, KeycloakRole> assignedRoles = kcRoleRepository.findRolesIn(
                dao.getDetails().stream().map(RoleGroupDetailDto::getRoleId).collect(Collectors.toSet())
        );
        keycloakClient.postRealmMappings(UserManagementConstant.BEARER_TOKEN + kcAdminRepository.getAdminAccessToken(),
                realm,
                group.getId(),
                assignedRoles.values().stream().map(item -> helper.convertToDto(item)).toList()
        );
        Stream<KcGroupDetail> details = assignedRoles.values().stream()
                .map(item -> new KcGroupDetail(group, item));
        groupDetailRepository.persist(details);

        dao = helper.convertToDto(group);
        dao.setDetails(groupDetailRepository.findByGroup(dao.getId()).stream().map(helper::convertToDto).toList());
        return dao;
    }
}
