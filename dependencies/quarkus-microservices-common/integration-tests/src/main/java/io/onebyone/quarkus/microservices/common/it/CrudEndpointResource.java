
package io.onebyone.quarkus.microservices.common.it;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import java.net.URI;

@Path("/")
@ApplicationScoped
public class CrudEndpointResource {
    // add some rest methods here

    @GET
    public Response hello() {
        return Response.temporaryRedirect(URI.create("q/swagger-ui")).build();
    }
}
