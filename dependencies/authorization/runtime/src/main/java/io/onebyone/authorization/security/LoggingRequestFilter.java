package io.onebyone.authorization.security;

import io.onebyone.authorization.logging.RequestLogData;
import io.onebyone.authorization.logging.RequestLoggingListener;
import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.HttpHeaders;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;

@PreMatching
@Singleton
public class LoggingRequestFilter implements ContainerResponseFilter {

    @Inject
    Logger logger;
    @Inject
    RoutingContext routingContext;

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext responseContext) throws IOException {
        RequestLogData data = new RequestLogData();
        data.timestamp = ZonedDateTime.now();
        data.method = containerRequestContext.getMethod();
        data.ipAddress = routingContext.request().remoteAddress().toString();
        data.uri = routingContext.request().uri();
        data.status = responseContext.getStatus();
        data.userAgent = containerRequestContext.getHeaderString(HttpHeaders.USER_AGENT);

        if (containerRequestContext.getSecurityContext().getUserPrincipal() != null) {
            data.principalName = containerRequestContext.getSecurityContext().getUserPrincipal().getName();
        } else {
            data.principalName = "Unauthenticated";
        }
        logger.info(data.ipAddress + "--> " + data.method + " " + data.uri + ", by:" +
                data.principalName + " <-- " + data.status +
                ", Agent:" + data.userAgent);

        RequestLoggingListener loggingListener = getRequestLoggingListener();
        if (loggingListener != null) {
            loggingListener.onLogging(data);
        }
    }

    private RequestLoggingListener getRequestLoggingListener() {
        try (InstanceHandle<RequestLoggingListener> instance = Arc.container().instance(RequestLoggingListener.class)){
            if (instance.isAvailable()) {
                return instance.get();
            }
        } catch (Exception e) {
            //
        }
        return null;
    }
}
