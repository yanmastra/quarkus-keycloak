package io.yanmastra.authentication.it;

import io.yanmastra.authentication.logging.RequestLogData;
import io.yanmastra.authentication.logging.RequestLoggingListener;
import io.yanmastra.quarkusBase.utils.JsonUtils;
import io.yanmastra.quarkusBase.utils.KeyValueCacheUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class TestRequestLoggingListener implements RequestLoggingListener {
    @Inject
    Logger log;

    @Override
    public void onLogging(RequestLogData logData) {
        String logging = JsonUtils.toJson(logData);
        log.error("data:"+logging);
        KeyValueCacheUtils.saveCache("test-log", logData.timestamp.toInstant().toEpochMilli()+"", logging);
    }
}
