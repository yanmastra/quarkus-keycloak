package io.yanmastra.authorization.security;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.HttpHeaders;
import org.jboss.logging.Logger;

import java.io.IOException;

@PreMatching
@Singleton
public class LoggingRequestFilter implements ContainerRequestFilter {

    @Inject
    Logger logger;

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        if (containerRequestContext.getSecurityContext().getUserPrincipal() != null) {
            logger.info(containerRequestContext.getMethod() + " " + containerRequestContext.getUriInfo().getRequestUri() + ", by:" +
                    containerRequestContext.getSecurityContext().getUserPrincipal().getName() +
                    ", Agent:" + containerRequestContext.getHeaderString(HttpHeaders.USER_AGENT)
            );
        } else {
            logger.info(containerRequestContext.getMethod() + " " + containerRequestContext.getUriInfo().getRequestUri() + ", by: public" +
                    ", Agent:" + containerRequestContext.getHeaderString(HttpHeaders.USER_AGENT));
        }
    }
}
