package io.yanmastra.authentication.service;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.vertx.core.MultiMap;
import io.yanmastra.authentication.logging.RequestLogData;
import io.yanmastra.authentication.payload.UserTokenPayload;

public interface SecurityLifeCycleService {
    default UserTokenPayload onCreateAccessTokenPayload(String userId){
        return null;
    }

    default boolean isSkipAuthorisation(String path){
        return false;
    }

    default boolean isSkipLogging(String path){
        return false;
    }
    default void onLogging(RequestLogData logData){}
    default ChallengeData onUnauthorizedError(String requestPath, MultiMap headers) {
        return new ChallengeData(HttpResponseStatus.UNAUTHORIZED.code(), null, null);
    }
}
