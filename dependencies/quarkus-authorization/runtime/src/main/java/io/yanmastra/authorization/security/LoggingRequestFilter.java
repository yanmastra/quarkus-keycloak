package io.yanmastra.authorization.security;

import io.vertx.ext.web.RoutingContext;
import io.yanmastra.authentication.logging.RequestLogData;
import io.yanmastra.authorization.logging.RequestLoggingListener;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.HttpHeaders;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.stream.Stream;

@PreMatching
@Singleton
public class LoggingRequestFilter implements ContainerResponseFilter {

    @Inject
    Logger logger;
    @Inject
    RoutingContext routingContext;
    @Inject
    Instance<RequestLoggingListener> requestLoggingListenerBeans;

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
        try (Stream<RequestLoggingListener> errorMapperStream = requestLoggingListenerBeans.stream()) {
            return errorMapperStream.findFirst().orElse(null);
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
        return null;
    }
}
