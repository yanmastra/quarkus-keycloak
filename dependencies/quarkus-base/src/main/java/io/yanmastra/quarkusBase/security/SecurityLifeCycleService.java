package io.yanmastra.quarkusBase.security;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.vertx.core.MultiMap;
import io.yanmastra.quarkusBase.RequestLogData;

public interface SecurityLifeCycleService {
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
