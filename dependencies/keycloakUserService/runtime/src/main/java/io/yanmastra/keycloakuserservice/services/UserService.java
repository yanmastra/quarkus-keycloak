package io.yanmastra.keycloakuserservice.services;

import io.yanmastra.authorization.ResponseJson;
import io.yanmastra.authorization.security.UserPrincipal;
import io.yanmastra.keycloakuserservice.dto.ResetPasswordPayload;
import io.yanmastra.keycloakuserservice.dto.UserDto;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

public interface UserService {
    void createUserToKeycloak(UserDto dao, UserPrincipal principal);

    void triggerKeycloakOnDelete(UserDto dao, UserPrincipal principal);

    void sendVerificationOrResetPasswordEmail(UserDto dao, String password, UserPrincipal principal);

    ResponseJson<UserDto> createUser(UserDto dao, SecurityContext context);

    UserDto getUser(String userId);

    UserDto getUserByToken(String token);

    Response updatePassword(ResetPasswordPayload payload, UserPrincipal principal);
}
