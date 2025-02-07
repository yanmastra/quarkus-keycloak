package io.onebyone.authentication.security;

import io.onebyone.authentication.logging.RequestLogData;
import io.onebyone.authentication.logging.RequestLoggingListener;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.HttpHeaders;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import java.io.*;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

@PreMatching
@Singleton
public class LoggingRequestFilter implements ContainerResponseFilter {

    @Inject
    Logger logger;
    @Inject RoutingContext routingContext;
    @Inject
    Instance<RequestLoggingListener> requestLoggingListenerBeans;
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    private String getIP(ContainerRequestContext containerRequestContext) {
        String hostAddress = routingContext.request().remoteAddress().hostAddress();
        if (StringUtils.isNotBlank(hostAddress)) {
            return hostAddress;
        }

        List<String> forwardedFor = containerRequestContext.getHeaders().get("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.getFirst().split(",")[0].trim();
        }
        return containerRequestContext.getUriInfo().getRequestUri().getHost();
    }

    private RequestLoggingListener getRequestLoggingListener() {
        try (Stream<RequestLoggingListener> errorMapperStream = requestLoggingListenerBeans.stream()) {
            return errorMapperStream.findFirst().orElse(null);
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
        return null;
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
        RequestLogData data = new RequestLogData();
        data.timestamp = ZonedDateTime.now();
        data.method = containerRequestContext.getMethod();
        data.ipAddress = getIP(containerRequestContext);
        data.uri = containerRequestContext.getUriInfo().getPath();
        data.status = containerResponseContext.getStatus();
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
            executorService.submit(() -> loggingListener.onLogging(data));
        }
    }
}
