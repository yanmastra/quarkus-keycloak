package io.yanmastra.authentication.service;

import io.yanmastra.authentication.payload.UserTokenPayload;

public interface UserService {
    UserTokenPayload getAccessTokenPayload(String userId);
}
