package io.yanmastra.quarkusBase.logging;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import io.yanmastra.quarkusBase.security.SecurityLifeCycleService;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.HttpHeaders;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoggingRequestFilter implements ContainerResponseFilter {
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
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
        SecurityLifeCycleService requestLoggingListener = getRequestLoggingListener();
        if (requestLoggingListener != null && requestLoggingListener.isSkipLogging(containerRequestContext.getUriInfo().getPath())) {
            return;
        }

        io.yanmastra.quarkusBase.RequestLogData data = new io.yanmastra.quarkusBase.RequestLogData();
        data.timestamp = ZonedDateTime.now();
        data.method = containerRequestContext.getMethod();
        data.ipAddress = getIP(containerRequestContext);
        data.uri = containerRequestContext.getUriInfo().getPath();
        data.userAgent = containerRequestContext.getHeaderString(HttpHeaders.USER_AGENT);
        data.status = containerResponseContext.getStatus();

        if (containerRequestContext.getSecurityContext().getUserPrincipal() != null) {
            data.principalName = containerRequestContext.getSecurityContext().getUserPrincipal().getName();
        } else {
            data.principalName = "Unauthenticated";
        }

        log.info(data.ipAddress + "--> " + data.method + " " + data.uri + ", by:" +
                data.principalName + " <-- " + data.status +
                ", Agent:" + data.userAgent);

        SecurityLifeCycleService loggingListener = getRequestLoggingListener();
        if (loggingListener != null) {
            virtualExecService.submit(() -> loggingListener.onLogging(data));
        }
    }
}
