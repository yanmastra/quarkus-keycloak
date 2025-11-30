package io.yanmastra.authentication.logging;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import io.smallrye.common.annotation.Blocking;
import io.yanmastra.authentication.service.SecurityLifeCycleService;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.HttpHeaders;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;

@PreMatching
public class LoggingRequestFilter implements ContainerRequestFilter {
    private static final Logger LOG = Logger.getLogger(LoggingRequestFilter.class);

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
            LOG.warn(e.getMessage());
        }
        return null;
    }

    @Blocking
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

        LOG.info(data.ipAddress + "--> " + data.method + " " + data.uri + ", by:" +
                data.principalName + " <-- " + data.status +
                ", Agent:" + data.userAgent);

        try {
            InputStream is = containerRequestContext.getEntityStream();
            byte[] contents = is.readAllBytes();
            is.close();
            containerRequestContext.setEntityStream(new ByteArrayInputStream(contents));
            String body = new String(contents);
            data.requestPayload = body;
            LOG.info("body: " + body);
        } catch (Exception e) {
            LOG.error(e.getMessage(),  e);
        }

        SecurityLifeCycleService loggingListener = getRequestLoggingListener();
        if (loggingListener != null) {
            Arc.container().getExecutorService().submit(() -> {
                loggingListener.onLogging(data);
            });
        }
    }
}
