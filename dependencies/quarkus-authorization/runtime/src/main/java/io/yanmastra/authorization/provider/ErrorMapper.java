package io.yanmastra.authorization.provider;

import io.quarkus.runtime.util.StringUtil;
import io.vertx.ext.web.handler.HttpException;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
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

import java.util.Optional;
import java.util.stream.Stream;

@Singleton
public class ErrorMapper implements ResteasyReactiveAsyncExceptionMapper<Exception>, ResteasyReactiveExceptionMapper<Exception> {

    @Inject
    Logger logger;
    @Inject
    ContainerRequestContext requestContext;

    @Inject
    Instance<HtmlErrorMapper> htmlErrorMappers;

    @Override
    public void asyncResponse(Exception exception, AsyncExceptionMapperContext context) {
        Response response = this.toResponse(exception, context.serverRequestContext());
        context.setResponse(response);
    }

    @Override
    public Response toResponse(Exception exception, ServerRequestContext context) {
        logger.error(requestContext.getUriInfo().getPath() + "::" + exception.getMessage(), exception, exception.getCause());

        String message = null;
        int status = 500;

        MultivaluedMap<String, String> headers = requestContext.getHeaders();
        if ((headers.containsKey(HttpHeaders.ACCEPT) && headers.getFirst(HttpHeaders.ACCEPT).equals(MediaType.APPLICATION_JSON)) ||
                (headers.containsKey(HttpHeaders.CONTENT_TYPE) && headers.getFirst(HttpHeaders.CONTENT_TYPE).equals(MediaType.APPLICATION_JSON)) ||
                htmlErrorMappers.stream().findAny().isEmpty()
        ) {
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


        try (Stream<HtmlErrorMapper> errorMapperStream = htmlErrorMappers.stream()) {
            Optional<HtmlErrorMapper> errorMapper = errorMapperStream.findFirst();
            return errorMapper.map(mapper -> mapper.getResponse(exception)).orElse(null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
}
