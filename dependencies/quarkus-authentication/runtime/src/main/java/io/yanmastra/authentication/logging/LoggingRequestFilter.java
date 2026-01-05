package io.yanmastra.authentication.logging;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import io.yanmastra.authentication.service.SecurityLifeCycleService;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@PreMatching
public class LoggingRequestFilter implements ContainerRequestFilter {
    private static final Logger log = Logger.getLogger(LoggingRequestFilter.class);
    private static final ExecutorService virtualExecService = Executors.newVirtualThreadPerTaskExecutor();

    private String getIP(ContainerRequestContext containerRequestContext) {
        String realIp = containerRequestContext.getHeaderString("X-Real-IP");
        if (StringUtils.isNotBlank(realIp)) {
            return realIp;
        }

        String forwardedFor = containerRequestContext.getHeaderString("X-Forwarded-For");
        if (StringUtils.isNotBlank(forwardedFor)) {
            return forwardedFor;
        }

        String hostAddress = containerRequestContext.getHeaderString("X-Forwarded-Host");
        if (StringUtils.isNotBlank(hostAddress)) {
            return hostAddress;
        }
        return containerRequestContext.getUriInfo().getRequestUri().getHost();
    }

    private SecurityLifeCycleService getRequestLoggingListener() {
        try (InstanceHandle<SecurityLifeCycleService> errorMapperStream = Arc.container().beanInstanceSupplier(SecurityLifeCycleService.class).get()) {
            return errorMapperStream.orElse(null);
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        return null;
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        SecurityLifeCycleService requestLoggingListener = getRequestLoggingListener();
        if (requestLoggingListener != null && requestLoggingListener.isSkipLogging(containerRequestContext.getUriInfo().getPath())) {
            return;
        }

        RequestLogData data = new RequestLogData();
        data.timestamp = ZonedDateTime.now();
        data.method = containerRequestContext.getMethod();
        data.ipAddress = getIP(containerRequestContext);
        data.uri = containerRequestContext.getUriInfo().getPath();
//        data.status = containerResponseContext.getStatus();
        data.userAgent = containerRequestContext.getHeaderString(HttpHeaders.USER_AGENT);

        if (containerRequestContext.getSecurityContext().getUserPrincipal() != null) {
            data.principalName = containerRequestContext.getSecurityContext().getUserPrincipal().getName();
        } else {
            data.principalName = "Unauthenticated";
        }

        log.info(data.ipAddress + "--> " + data.method + " " + data.uri + ", by:" +
                data.principalName + " <-- " + data.status +
                ", Agent:" + data.userAgent);

        if (MediaType.APPLICATION_JSON_TYPE.isCompatible(containerRequestContext.getMediaType()) ||
                MediaType.TEXT_PLAIN_TYPE.isCompatible(containerRequestContext.getMediaType()) ||
                MediaType.APPLICATION_FORM_URLENCODED_TYPE.isCompatible(containerRequestContext.getMediaType())) {
            try {
                String body = new String(containerRequestContext.getEntityStream().readAllBytes(), StandardCharsets.UTF_8);
                containerRequestContext.setEntityStream(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
                data.requestPayload = body;
                log.info("body: " + body);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        SecurityLifeCycleService loggingListener = getRequestLoggingListener();
        if (loggingListener != null) {
            virtualExecService.submit(() -> loggingListener.onLogging(data));
        }
    }
}
