package io.yanmastra.keycloakuserservice;

import io.yanmastra.authorization.security.UserPrincipal;
import io.yanmastra.keycloakuserservice.data.entities.*;
import io.yanmastra.keycloakuserservice.dto.*;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class Helper {

    @Inject
    SecurityIdentity securityIdentity;
    @ConfigProperty(name = "quarkus.profile")
    String profile;
    @Inject
    Logger log;

    public String getCompanyId() {
        log.info("profile: "+profile);

        if ("dev".equals(profile)) {
            return "cedea";
        }

        String companyId = null;
        Principal principal = securityIdentity.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            companyId = userPrincipal.companyAccess().stream().findAny().orElse(null);
        }
        if (companyId == null && !"dev".equals(profile))
            throw new ForbiddenException("You do not have a company access");

        log.debug("companyId: "+companyId);
        return companyId;
    }


    public KeycloakRole convertToEntity(RoleRepresentation representation) {
        KeycloakRole role = new KeycloakRole();
        role.setId(representation.getId());
        role.setName(representation.getName());
        role.setDescription(representation.getDescription());
        return role;
    }

    public KeycloakRoleGroup convertToEntity(GroupRepresentation representation) {
        KeycloakRoleGroup group = new KeycloakRoleGroup();
        group.setId(representation.getId());
        group.setName(representation.getName());
        group.setPath(representation.getPath());
        group.setSubGroupCount(representation.getSubGroupCount());
        if (StringUtils.isNotBlank(representation.getParentId())) {
            group.setParent(new KeycloakRoleGroup(representation.getParentId()));
            group.setParentId(representation.getParentId());
        }
        return group;
    }

    public KeycloakRoleGroup convertToEntity(RoleGroupDto representation) {
        KeycloakRoleGroup group = new KeycloakRoleGroup();
        group.setId(representation.getId());
        group.setName(representation.getName());
        group.setPath(representation.getPath());
        group.setSubGroupCount(representation.getSubGroupCount());

        if (StringUtils.isNotBlank(representation.getParentId())) {
            group.setParent(new KeycloakRoleGroup(representation.getParentId()));
            group.setParentId(representation.getParentId());
        }
        return group;
    }

    public RoleRepresentation convertToDto(KeycloakRole entity) {
        RoleRepresentation role = new RoleRepresentation();
        role.setId(entity.getId());
        role.setName(entity.getName());
        role.setDescription(entity.getDescription());
        return role;
    }

    public RoleGroupDto convertToDto(KeycloakRoleGroup entity) {
        RoleGroupMappingRoleDto group = new RoleGroupMappingRoleDto();
        group.setId(entity.getId());
        group.setName(entity.getName());
        group.setPath(entity.getPath());
        group.setSubGroupCount(entity.getSubGroupCount());
        group.setLabel(entity.getLabel());
        if (entity.getParent() != null) {
            group.setParentId(entity.getParent().getId());
        }
        return group;
    }

    public RoleGroupDetailDto convertToDto(KcGroupDetail item) {
        return new RoleGroupDetailDto(item.getId(), item.getGroup().getId(), item.getRole().getId());
    }

    public UserDto convertToDto(User user) {
        UserDto userDto = new UserDto();
        userDto.id = user.getId();
        userDto.username = user.getUsername();
        userDto.email = user.getEmail();
        userDto.fullName = user.getFullName();
        userDto.contactPhone = user.getContactPhone();
        userDto.contactMobile = user.getContactMobile();
        userDto.address = user.getAddress();
        userDto.country = user.getCountry();
        userDto.state = user.getState();
        userDto.city = user.getCity();
        userDto.emergencyContactName = user.getEmergencyContactName();
        userDto.emergencyContactPhone = user.getEmergencyContactPhone();
        userDto.isVerified = user.isVerified();

        if (user.getGroups() != null && !user.getGroups().isEmpty()) {
            userDto.groups = user.getGroups().stream().map(
                    item -> convertToDto(item.getGroup())
            ).toList();
        }
        return userDto;
    }

    public User convertToEntity(UserDto dao) {
        User user = new User();
        user.setId(dao.id);
        user.setUsername(dao.username);
        user.setEmail(dao.email);
        user.setName(dao.fullName);
        user.setAddress(dao.address);
        user.setCountry(dao.country);
        user.setCity(dao.city);
        user.setEmergencyContactName(dao.emergencyContactName);
        user.setEmergencyContactPhone(dao.emergencyContactPhone);
        user.setJobTitle(dao.jobTitle);
        user.setJobCategory(dao.jobCategory);
        user.setState(dao.state);
        user.setContactPhone(dao.contactPhone);
        user.setContactMobile(dao.contactMobile);
        user.setVerified(dao.isVerified != null && dao.isVerified);

        if (dao.groups != null && !dao.groups.isEmpty()) {
            if (StringUtils.isNotBlank(dao.id)) {
                List<UserRoleGroup> userRoles = UserRoleGroup.find("where user.id=?1", dao.id).list();
                user.setGroups(userRoles);
            }
        }
        return user;
    }

    public User convertToEntity(UserRepresentation dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFirstName()+" "+dto.getLastName());
        user.setVerified(dto.isEmailVerified());
        return user;
    }

    public SimpleAppFeatureDto convertToDto(AppFeature entity) {
        return new SimpleAppFeatureDto(
                entity.getId(),
                entity.getLabel(),
                entity.getRealmName(),
                entity.getFeatureKey(),
                new ArrayList<>()
        );
    }
}
