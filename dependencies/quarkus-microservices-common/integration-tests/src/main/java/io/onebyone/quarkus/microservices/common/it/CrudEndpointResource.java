
package io.onebyone.quarkus.microservices.common.it;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/crud-endpoint")
@ApplicationScoped
public class CrudEndpointResource {
    // add some rest methods here

    @GET
    public String hello() {
        return "Hello crud-endpoint";
    }
}
