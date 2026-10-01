package io.yanmastra.quarkus.rediscache.it;

import io.yanmastra.authentication.payload.UserTokenPayload;
import io.yanmastra.authentication.service.SecurityLifeCycleService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TestSecurityLifeCycle implements SecurityLifeCycleService {

    @Override
    public UserTokenPayload onCreateAccessTokenPayload(String userId) {
        return new TestUser();
    }

    @Override
    public boolean isSkipAuthorisation(String path) {
        return false;
    }

    @Override
    public boolean isSkipLogging(String path) {
        return false;
    }
}
