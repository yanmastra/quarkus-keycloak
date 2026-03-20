package io.yanmastra.quarkusBase.provider;

import io.quarkus.runtime.util.StringUtil;
import io.vertx.ext.web.handler.HttpException;
import io.yanmastra.quarkusBase.event.ErrorEvent;
import io.yanmastra.quarkusBase.exception.ValidationErrorException;
import io.yanmastra.quarkusBase.utils.ValidationUtils;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.Instance;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.spi.AsyncExceptionMapperContext;
import org.jboss.resteasy.reactive.server.spi.ResteasyReactiveAsyncExceptionMapper;
import org.jboss.resteasy.reactive.server.spi.ResteasyReactiveExceptionMapper;
import org.jboss.resteasy.reactive.server.spi.ServerRequestContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class ErrorMapper implements ResteasyReactiveAsyncExceptionMapper<Exception>, ResteasyReactiveExceptionMapper<Exception> {
    private static final Log log = LogFactory.getLog(ErrorMapper.class);
    @Inject
    Logger logger;
    @Inject
    ContainerRequestContext requestContext;

    @Inject
    Instance<HtmlErrorMapper> htmlErrorMappers;

    @Inject
    Event<ErrorEvent> errorEvent;

    @Override
    public void asyncResponse(Exception exception, AsyncExceptionMapperContext context) {
        Response response = this.toResponse(exception, context.serverRequestContext());
        context.setResponse(response);
    }

    @Override
    public Response toResponse(Exception exception, ServerRequestContext context) {

        String message = null;
        int status = 500;
        Map<String, String> errors = new HashMap<>();

        MultivaluedMap<String, String> headers = requestContext.getHeaders();
        if ((headers.containsKey(HttpHeaders.ACCEPT) && headers.getFirst(HttpHeaders.ACCEPT).equals(MediaType.APPLICATION_JSON)) ||
                (headers.containsKey(HttpHeaders.CONTENT_TYPE) && headers.getFirst(HttpHeaders.CONTENT_TYPE).equals(MediaType.APPLICATION_JSON)) ||
                htmlErrorMappers.stream().findAny().isEmpty()
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
                    ValidationUtils.fetchValidationError(clientError, errors);
                }
                case ValidationErrorException validationError -> {
                    message = validationError.getMessage();
                    status = 400;
                    errors.putAll(validationError.getErrors());
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

            try {
                errorEvent.fire(new ErrorEvent(exception, status, requestContext.getUriInfo().getPath(), message));
            } catch (Exception e) {
                logger.error("Failed to fire error event", e);
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

        try (Stream<HtmlErrorMapper> errorMapperStream = htmlErrorMappers.stream()) {
            Optional<HtmlErrorMapper> errorMapper = errorMapperStream.findFirst();
            return errorMapper.map(mapper -> mapper.getResponse(exception)).orElse(null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
}
