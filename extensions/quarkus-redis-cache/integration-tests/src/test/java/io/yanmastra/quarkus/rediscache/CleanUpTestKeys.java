package io.yanmastra.quarkus.rediscache;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyScanArgs;
import io.quarkus.redis.datasource.keys.KeyScanCursor;
import io.quarkus.runtime.ShutdownEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

/**
 * The tests may run against a long-lived Redis (see application.properties), so remove what they wrote when the test
 * application stops. Only keys under the test prefix are touched.
 */
@ApplicationScoped
public class CleanUpTestKeys {

    void onStop(@Observes ShutdownEvent event, RedisDataSource redis, RedisCacheConfig config) {
        String prefix = config.keyPrefix();
        if (!prefix.endsWith("-it")) return; // never delete real data

        KeyScanCursor<String> cursor = redis.key().scan(new KeyScanArgs().match(prefix + ":*").count(100));
        while (cursor.hasNext()) {
            for (String key : cursor.next()) {
                redis.key().del(key);
            }
        }
    }
}
