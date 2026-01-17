package io.yanmastra.quarkus.microservices.common.it;

import io.quarkus.runtime.util.StringUtil;
import io.vertx.ext.web.handler.HttpException;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.spi.AsyncExceptionMapperContext;
import org.jboss.resteasy.reactive.server.spi.ResteasyReactiveAsyncExceptionMapper;
import org.jboss.resteasy.reactive.server.spi.ResteasyReactiveExceptionMapper;
import org.jboss.resteasy.reactive.server.spi.ServerRequestContext;

//@Provider
public class ErrorMapper implements ResteasyReactiveAsyncExceptionMapper<Exception>, ResteasyReactiveExceptionMapper<Exception> {

    @Inject
    Logger logger;
    @Inject
    ContainerRequestContext requestContext;

    @Override
    public void asyncResponse(Exception exception, AsyncExceptionMapperContext context) {
        Response response = this.toResponse(exception, context.serverRequestContext());
        context.setResponse(response);
    }

    @Override
    public Response toResponse(Exception exception, ServerRequestContext context) {

        String message = null;
        int status = 500;

        MultivaluedMap<String, String> headers = requestContext.getHeaders();
        if ((headers.containsKey(HttpHeaders.ACCEPT) && headers.getFirst(HttpHeaders.ACCEPT).equals(MediaType.APPLICATION_JSON)) ||
                (headers.containsKey(HttpHeaders.CONTENT_TYPE) && headers.getFirst(HttpHeaders.CONTENT_TYPE).equals(MediaType.APPLICATION_JSON))
        ) {
            if (requestContext.getUriInfo().getPath().startsWith("/assets")) {
                logger.error(requestContext.getUriInfo().getPath() + "::" + exception.getMessage());
            } else
                logger.error(requestContext.getUriInfo().getPath() + "::" + exception.getMessage(), exception.getCause() == null ? exception : exception.getCause());

            switch (exception) {
                case HttpException httpException -> {
                    message = httpException.getPayload();
                    status = httpException.getStatusCode();
                }
                case ClientErrorException clientError -> {
                    message = clientError.getMessage();
                    status = clientError.getResponse().getStatus();
                }
                case SecurityException securityException -> {
                    message = securityException.getMessage();
                    status = 403;
                }
                default -> {
                    Throwable cause = exception.getCause();
                    message = cause == null ? exception.getMessage() : cause.getMessage();
                }
            }

            JsonObjectBuilder job = Json.createObjectBuilder()
                    .add("success", false);
            if (StringUtil.isNullOrEmpty(message)) {
                job.add("message", JsonValue.NULL);
            } else {
                job.add("message", message);
            }
            String responsePayload = job.build().toString();

            return Response.status(status)
                    .entity(responsePayload)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        return null;
    }
}
