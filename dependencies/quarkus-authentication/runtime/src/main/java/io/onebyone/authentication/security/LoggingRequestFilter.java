package io.onebyone.authentication.security;

import io.onebyone.authentication.logging.RequestLogData;
import io.onebyone.authentication.logging.RequestLoggingListener;
import io.vertx.ext.web.RoutingContext;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private ExecutorService executorService = null;

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext responseContext) throws IOException {
        if (executorService == null) executorService = Executors.newFixedThreadPool(2);

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
            executorService.submit(() -> {
                byte[] payloadByte = new byte[routingContext.body().length()];
                routingContext.body().buffer().getBytes(payloadByte);
                data.requestPayload = new String(payloadByte);
                loggingListener.onLogging(data);
            });
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
