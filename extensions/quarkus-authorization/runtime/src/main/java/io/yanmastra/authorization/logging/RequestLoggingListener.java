package io.yanmastra.authorization.logging;


import io.yanmastra.quarkusBase.RequestLogData;

public interface RequestLoggingListener {
    void onLogging(RequestLogData logData);
}
