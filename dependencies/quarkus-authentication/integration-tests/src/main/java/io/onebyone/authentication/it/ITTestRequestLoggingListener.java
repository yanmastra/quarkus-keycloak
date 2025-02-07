package io.onebyone.authentication.it;

import io.onebyone.authentication.logging.RequestLogData;
import io.onebyone.authentication.logging.RequestLoggingListener;
import io.onebyone.authentication.utils.JsonUtils;
import io.onebyone.authentication.utils.KeyValueCacheUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ITTestRequestLoggingListener implements RequestLoggingListener {
    @Inject
    Logger log;

    @Override
    public void onLogging(RequestLogData logData) {
        String logging = JsonUtils.toJson(logData);
        log.error("data:"+logging);
        KeyValueCacheUtils.saveCache("test-log", logData.timestamp.toInstant().toEpochMilli()+"", logging);
    }
}
