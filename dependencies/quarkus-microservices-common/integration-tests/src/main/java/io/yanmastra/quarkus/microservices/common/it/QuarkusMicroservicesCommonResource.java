
package io.yanmastra.quarkus.microservices.common.it;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/quarkus-microservices-common")
@ApplicationScoped
public class QuarkusMicroservicesCommonResource {
    // add some rest methods here

    @GET
    public String hello() {
        return "Hello quarkus-microservices-common";
    }
}
