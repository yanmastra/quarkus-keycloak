package io.yanmastra.authentication.service;

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
}
