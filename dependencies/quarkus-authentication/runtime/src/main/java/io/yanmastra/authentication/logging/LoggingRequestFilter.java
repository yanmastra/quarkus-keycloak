package io.yanmastra.authentication.logging;

import io.vertx.ext.web.RoutingContext;
import io.yanmastra.authentication.service.SecurityLifeCycleService;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.HttpHeaders;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

@PreMatching
@Singleton
public class LoggingRequestFilter implements ContainerResponseFilter {

    private static final Log log = LogFactory.getLog(LoggingRequestFilter.class);
    @Inject
    Logger logger;
    @Inject RoutingContext routingContext;
    @Inject
    Instance<SecurityLifeCycleService> securityLifeCycleServiceInstance;
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    private String getIP(ContainerRequestContext containerRequestContext) {
        String realIp = containerRequestContext.getHeaderString("X-Real-IP");
        if (StringUtils.isNotBlank(realIp)) {
            return realIp;
        }

        String forwardedFor = containerRequestContext.getHeaderString("X-Forwarded-For");
        if (StringUtils.isNotBlank(forwardedFor)) {
            return forwardedFor;
        }

        String hostAddress = routingContext.request().remoteAddress().hostAddress();
        if (StringUtils.isNotBlank(hostAddress)) {
            return hostAddress;
        }
        return containerRequestContext.getUriInfo().getRequestUri().getHost();
    }

    private SecurityLifeCycleService getRequestLoggingListener() {
        try (Stream<SecurityLifeCycleService> errorMapperStream = securityLifeCycleServiceInstance.stream()) {
            return errorMapperStream.findFirst().orElse(null);
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
        return null;
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
        Optional<SecurityLifeCycleService> opsSecLifeCycleService = securityLifeCycleServiceInstance.stream().findFirst();
        if (opsSecLifeCycleService.isPresent() && opsSecLifeCycleService.get().isSkipLogging(routingContext.request().path())) {
            return;
        }

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

        SecurityLifeCycleService loggingListener = getRequestLoggingListener();
        if (loggingListener != null) {
            executorService.submit(() -> loggingListener.onLogging(data));
        }
    }
}
