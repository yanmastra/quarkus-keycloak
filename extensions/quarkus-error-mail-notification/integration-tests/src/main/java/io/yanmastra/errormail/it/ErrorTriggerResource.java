package io.yanmastra.errormail.it;

import io.vertx.ext.web.handler.HttpException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/test")
@Produces(MediaType.APPLICATION_JSON)
public class ErrorTriggerResource {

    @GET
    @Path("/ok")
    public String ok() {
        return "{\"status\":\"ok\"}";
    }

    @GET
    @Path("/server-error")
    public String serverError() {
        throw new RuntimeException("Simulated internal server error");
    }

    @GET
    @Path("/client-error")
    public String clientError() {
        throw new HttpException(400, "Simulated bad request");
    }

    @GET
    @Path("/forbidden")
    public String forbidden() {
        throw new SecurityException("Simulated forbidden");
    }

    @GET
    @Path("/another-server-error")
    public String anotherServerError() {
        throw new IllegalStateException("Another simulated server error");
    }

    @GET
    @Path("/cooldown-error")
    public String cooldownError() {
        throw new RuntimeException("Cooldown test error");
    }

    @GET
    @Path("/cooldown-error-different")
    public String cooldownErrorDifferent() {
        throw new IllegalArgumentException("Cooldown test different error");
    }
}
