package io.yanmastra.authentication.service;

import io.yanmastra.authentication.payload.UserTokenPayload;

public interface SecurityLifeCycleService extends io.yanmastra.quarkusBase.security.SecurityLifeCycleService {
    default UserTokenPayload onCreateAccessTokenPayload(String userId){
        return null;
    }
}
