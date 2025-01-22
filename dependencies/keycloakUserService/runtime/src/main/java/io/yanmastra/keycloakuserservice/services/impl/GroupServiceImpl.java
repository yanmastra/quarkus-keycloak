package io.yanmastra.keycloakuserservice.services.impl;

import io.yanmastra.keycloakuserservice.Helper;
import io.yanmastra.keycloakuserservice.data.AppFeatureRepository;
import io.yanmastra.keycloakuserservice.data.KcRoleGroupRepository;
import io.yanmastra.keycloakuserservice.data.KcRoleRepository;
import io.yanmastra.keycloakuserservice.data.entities.AppFeature;
import io.yanmastra.keycloakuserservice.data.entities.KeycloakRole;
import io.yanmastra.keycloakuserservice.data.entities.KeycloakRoleGroup;
import io.yanmastra.keycloakuserservice.dto.RoleGroupMappingRoleDto;
import io.yanmastra.keycloakuserservice.dto.SimpleAppFeatureDto;
import io.yanmastra.keycloakuserservice.dto.SimpleRoleDto;
import io.yanmastra.keycloakuserservice.services.GroupService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.*;

@ApplicationScoped
public class GroupServiceImpl implements GroupService {
    @Inject
    Logger log;
    @Inject
    KcRoleRepository roleRepository;
    @Inject
    KcRoleGroupRepository roleGroupRepository;
    @Inject
    AppFeatureRepository appFeatureRepository;
    @ConfigProperty(name = "keycloak_realm", defaultValue = "")
    String realm;
    @Inject
    Helper helper;

    @Override
    public void fetchGroupMapping(RoleGroupMappingRoleDto group) {
        KeycloakRoleGroup entity = roleGroupRepository.findById(group.id);
        fetchGroupMapping(group, entity);
    }

    @Override
    public void fetchGroupMapping(RoleGroupMappingRoleDto group, KeycloakRoleGroup entity) {
        Map<String, SimpleAppFeatureDto> roleMapping = new HashMap<>();
        Set<String> access = new HashSet<>(Set.of("offline_access", "uma_authorization"));

        if (entity.getGroupDetails() != null && !entity.getGroupDetails().isEmpty()) {
            entity.getGroupDetails().forEach(item -> {
                KeycloakRole role = item.getRole();
                fetchGroupRole(role, roleMapping, access, true);
            });

        }
        roleRepository.find("where name not in :names", Map.of("names", access)).stream().forEach(role -> {
            if (access.contains(role.getId())) return;
            fetchGroupRole(role, roleMapping, access, false);
        });
        group.setRoleMapping(roleMapping.values().stream().toList());
    }

    private void fetchGroupRole(KeycloakRole role, Map<String, SimpleAppFeatureDto> roleMapping, Set<String> access, boolean allowed) {
        if (role != null && StringUtils.isNotBlank(role.getName()) && role.getName().contains("_")) {
            SimpleAppFeatureDto moduleDto = null;
            access.add(role.getName());

            String moduleKey = role.getName().substring(0, role.getName().indexOf("_"));
            log.debug("moduleKey: "+moduleKey);
            if (roleMapping.containsKey(moduleKey)) {
                moduleDto = roleMapping.get(moduleKey);
            } else {
                AppFeature entity = appFeatureRepository.find("where realmName=:realm and featureKey=:key",
                        Map.of("realm", realm,
                                "key", moduleKey)
                ).firstResult();
                if (entity == null) {
                    entity = new AppFeature(
                            realm,
                            moduleKey,
                            moduleKey
                    );
                    appFeatureRepository.persist(entity);
                }
                moduleDto = helper.convertToDto(entity);
                moduleDto.accesses = new ArrayList<>();
                roleMapping.put(moduleKey, moduleDto);
            }

            SimpleRoleDto roleDto = new SimpleRoleDto();
            roleDto.name = role.getName();
            roleDto.description = role.getDescription();
            roleDto.id = role.getId();
            roleDto.isAllowed = allowed;
            moduleDto.accesses.add(roleDto);
        }
    }
}
