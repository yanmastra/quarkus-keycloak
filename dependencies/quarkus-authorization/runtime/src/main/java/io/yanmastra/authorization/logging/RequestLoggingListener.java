package io.yanmastra.authorization.logging;

import io.yanmastra.authentication.logging.RequestLogData;

public interface RequestLoggingListener {
    void onLogging(RequestLogData logData);
}
