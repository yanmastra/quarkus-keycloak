package io.yanmastra.keycloakuserservice.services.impl;

import io.yanmastra.authorization.ResponseJson;
import io.yanmastra.authorization.security.UserPrincipal;
import io.yanmastra.keycloakuserservice.Helper;
import io.yanmastra.keycloakuserservice.data.KcRoleGroupRepository;
import io.yanmastra.keycloakuserservice.data.KeycloakAdminRepository;
import io.yanmastra.keycloakuserservice.data.UserGroupRepository;
import io.yanmastra.keycloakuserservice.data.UsersRepository;
import io.yanmastra.keycloakuserservice.data.entities.KeycloakRoleGroup;
import io.yanmastra.keycloakuserservice.data.entities.User;
import io.yanmastra.keycloakuserservice.data.entities.UserRoleGroup;
import io.yanmastra.keycloakuserservice.dto.*;
import io.yanmastra.keycloakuserservice.services.UserService;
import io.yanmastra.keycloakuserservice.template.UserManagement;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class UserServiceImpl implements UserService {
    @Inject
    KeycloakAdminRepository adminRepository;
    @ConfigProperty(name = "quarkus.profile")
    String profile;
    @Inject
    KcRoleGroupRepository roleGroupRepo;
    @Inject
    Logger log;
    @Inject
    Mailer mailer;
    @ConfigProperty(name = "frontend_url")
    String frontEndUrl;
    @Inject
    Helper helper;
    @Inject
    UsersRepository userRepo;
    @Inject
    UserGroupRepository userGroupRepo;

    @Override
    public void createUserToKeycloak(UserDto dao, UserPrincipal principal) {

        if (dao.validate()) {
            String fistName = dao.fullName.substring(0, dao.fullName.indexOf(" "));
            String lastName = dao.fullName.substring(dao.fullName.indexOf(" ")+1);
            String companyId = "dev".equals(profile) ? "cedea" : principal.companyAccess().stream().findFirst().orElse(null);

            if (StringUtils.isBlank(dao.password)) {
                dao.password = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
            } else {
                if (dao.password.length() < 6) {
                    throw new IllegalArgumentException("Password must be at least 6 characters long");
                }
            }

            if (StringUtils.isBlank(companyId)) {
                throw new BadRequestException("Company ID is Empty");
            }

            List<String> groupPaths = getGroupPath(dao.groups, companyId);
            dao.id = adminRepository.createUser(dao.email, dao.username, fistName, lastName, companyId, dao.password, groupPaths);
        }
    }

    @Override
    public void triggerKeycloakOnDelete(UserDto dao, UserPrincipal principal) {
        adminRepository.deleteUser(dao.id);
        log.debug("User " + dao.username + " deleted");
    }

    @Override
    public void sendVerificationOrResetPasswordEmail(UserDto dao, String password, UserPrincipal principal) {
        if (StringUtils.isBlank(frontEndUrl)) {
            throw new InternalServerErrorException("Please set frontend url on application.properties");
        }

        String subject = dao.fullName + " | User verification";
        TemplateInstance instance = UserManagement.userResetPasswordMail(Map.of(
                "subject", subject,
                "user_name", dao.fullName,
                "email", dao.email,
                "password", StringUtils.isBlank(password) ? "" : password,
                "frontend_url", StringUtils.isBlank(frontEndUrl) ? "something": frontEndUrl
        ));
        String content = instance.render();
        log.debug("content: " + content);
        mailer.send(Mail.withHtml(dao.email, subject, content)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML));
    }

    @Override
    public ResponseJson<UserDto> createUser(UserDto dao, SecurityContext context) {
        User entity = helper.convertToEntity(dao);
        entity.setVerified(false);
        List<KeycloakRoleGroup> roleGroups = roleGroupRepo.findGroupsIn(dao.groups.stream().map(RoleGroupDto::getId).collect(Collectors.toSet()));
        userRepo.persist(entity);

        List<UserRoleGroup> groups = roleGroups.stream().map(item -> new UserRoleGroup(entity, item)).toList();
        log.debug("saving user group:"+groups.size());
        userGroupRepo.persist(groups.stream());

        entity.setGroups(groups);
        return new ResponseJson<>(helper.convertToDto(entity));
    }

    @Override
    public UserDto getUser(String userId) {
        User user = userRepo.findById(userId);
        if (user == null) throw new NotFoundException("User not found");
        return helper.convertToDto(user);
    }

    @Override
    public UserDto getUserByToken(String token) {
        KeycloakAccountRepresentation account = adminRepository.getSelfAccount(token);
        if (account == null) throw new RuntimeException("Something wrong when accessing Keycloak server!");
        return getUser(account.getId());
    }

    @Transactional
    @Override
    public Response updatePassword(ResetPasswordPayload payload, UserPrincipal principal) {
        try {
            UserRepresentation userRep = adminRepository.getKeycloakUser(principal.getUserId());

            CredentialRepresentation credPrep = new CredentialRepresentation();
            credPrep.setType(CredentialRepresentation.PASSWORD);
            credPrep.setValue(payload.newPassword);
            credPrep.setTemporary(false);
            userRep.setCredentials(List.of(credPrep));

            adminRepository.updateUser(principal.getUserId(), userRep);
            User entity = userRepo.findById(userRep.getId());
            entity.setVerified(true);
            return Response.noContent().build();
        } catch (Exception e){
            log.error(e.getMessage(), e);
            if (e instanceof ClientErrorException errorException) {
                throw errorException;
            } else {
                throw new InternalServerErrorException(e.getMessage(), e);
            }
        }
    }

    private List<String> getGroupPath(List<RoleGroupDto> groups, String companyId) {
        return groups.stream().map(item -> {
            KeycloakRoleGroup kcRoleGroup = roleGroupRepo.findUserGroupById(companyId, item.getId());
            if (kcRoleGroup == null) {
                log.error("Group " + item.getId() + " not found");
                throw new BadRequestException("Group " + item.getId() + " not found");
            }
            return kcRoleGroup.getPath();
        }).toList();
    }
}
