package io.yanmastra.authentication.security;

import io.yanmastra.authentication.logging.RequestLogData;
import io.yanmastra.authentication.logging.RequestLoggingListener;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import java.io.*;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

@PreMatching
@Singleton
public class LoggingRequestFilter implements ContainerRequestFilter {

    @Inject
    Logger logger;
    @Inject
    Instance<RequestLoggingListener> requestLoggingListenerBeans;
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        RequestLogData data = new RequestLogData();
        data.timestamp = ZonedDateTime.now();
        data.method = containerRequestContext.getMethod();
        data.ipAddress = getIP(containerRequestContext);
        data.uri = containerRequestContext.getUriInfo().getPath();
        data.status = 0;
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
            executorService.submit(() -> {
                if (containerRequestContext.hasEntity() && MediaType.APPLICATION_JSON.equals(containerRequestContext.getHeaderString(HttpHeaders.CONTENT_TYPE))) {
                    try {
                        byte[] payloadByte = containerRequestContext.getEntityStream().readAllBytes();
                        data.requestPayload = new String(payloadByte);

                        containerRequestContext.setEntityStream(new ByteArrayInputStream(payloadByte));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                loggingListener.onLogging(data);
            });
        }
    }

    private String getIP(ContainerRequestContext containerRequestContext) {
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
}
