package io.yanmastra.keycloakuserservice.resource;


import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.yanmastra.authorization.ResponseJson;
import io.yanmastra.authorization.security.UserPrincipal;
import io.yanmastra.keycloakuserservice.Helper;
import io.yanmastra.keycloakuserservice.data.UsersRepository;
import io.yanmastra.keycloakuserservice.data.entities.User;
import io.yanmastra.keycloakuserservice.dto.ResetPasswordPayload;
import io.yanmastra.keycloakuserservice.dto.UserDto;
import io.yanmastra.keycloakuserservice.services.UserService;
import io.yanmastra.quarkus.microservices.common.crud.CrudableEndpointResource;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.yanmastra.keycloakuserservice.UserManagementConstant.PATH_USERS;
import static io.yanmastra.keycloakuserservice.UserManagementConstant.PATH_USER_MANAGEMENT;

@Path(PATH_USER_MANAGEMENT + PATH_USERS)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource extends CrudableEndpointResource<User, UserDto> {
    private static final Logger log = LoggerFactory.getLogger(UserResource.class);
    @Inject
    UsersRepository repository;
    @Inject
    Helper helper;
    @Inject
    UserService userService;

    @Override
    protected PanacheRepositoryBase<User, String> getRepository() {
        return repository;
    }

    @Override
    protected UserDto fromEntity(User entity) {
        return helper.convertToDto(entity);
    }

    @Override
    protected User toEntity(UserDto dao) {
        return helper.convertToEntity(dao);
    }

    @Override
    protected User update(User entity, UserDto dao) {
        entity.setEmail(dao.email);
        entity.setName(dao.fullName);
        entity.setAddress(dao.address);
        entity.setCountry(dao.country);
        entity.setCity(dao.city);
        entity.setEmergencyContactName(dao.emergencyContactName);
        entity.setEmergencyContactPhone(dao.emergencyContactPhone);
        entity.setJobTitle(dao.jobTitle);
        entity.setJobCategory(dao.jobCategory);
        entity.setState(dao.state);
        entity.setContactPhone(dao.contactPhone);
        entity.setContactMobile(dao.contactMobile);
        return entity;
    }

    @Override
    public ResponseJson<UserDto> create(UserDto dao, SecurityContext context) throws Exception {
        UserPrincipal principal = context.getUserPrincipal() == null ? null : (UserPrincipal) context.getUserPrincipal();
        userService.createUserToKeycloak(dao, principal);
        try {
            String password = dao.password;
            ResponseJson<UserDto> response = userService.createUser(dao, context);
            userService.sendVerificationOrResetPasswordEmail(response.getData(), password, principal);
            return response;
        } catch (Exception e){
            log.error(e.getMessage(), e);
            userService.triggerKeycloakOnDelete(dao, context.getUserPrincipal() == null ? null : (UserPrincipal) context.getUserPrincipal());
            throw e;
        }
    }

    @POST
    @Path("reset-password")
    public Response resetPassword(ResetPasswordPayload payload, @Context SecurityContext context) {
        if (!payload.validate()) throw new BadRequestException("Password validation failed");
        if (context.getUserPrincipal() == null) throw new ForbiddenException();
        if (!(context.getUserPrincipal() instanceof UserPrincipal principal)) throw new InternalServerErrorException();

        return userService.updatePassword(payload, principal);
    }
}
