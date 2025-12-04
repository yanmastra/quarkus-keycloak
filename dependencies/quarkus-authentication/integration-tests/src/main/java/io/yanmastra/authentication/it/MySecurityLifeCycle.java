package io.yanmastra.authentication.it;

import io.yanmastra.authentication.payload.UserTokenPayload;
import io.yanmastra.authentication.service.SecurityLifeCycleService;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class MySecurityLifeCycle implements SecurityLifeCycleService {

    @Override
    public UserTokenPayload onCreateAccessTokenPayload(String userId) {
        AuthenticationResource.MyUserTokenPayload payload = new AuthenticationResource.MyUserTokenPayload();
        payload.id = UUID.randomUUID().toString();
        payload.username = "yanmastra";
        payload.email = "yanmastra61@gmail.com";
        payload.fullName = "Wayan Mastra";
        payload.permissions = Set.of(
                "view_all",
                "view_profile",
                "manage_users",
                "manage_user_permission"
        );
        payload.attributes = Map.of(
                "jumpcloud_token", UUID.randomUUID().toString(),
                "tenant_access", Set.of("mjl", "mrb", "mdn")
        );
        return payload;
    }

    @Override
    public boolean isSkipAuthorisation(String path) {
        return path.startsWith("/auth/test");
    }

    @Override
    public boolean isSkipLogging(String path) {
        return isSkipAuthorisation(path);
    }
}
