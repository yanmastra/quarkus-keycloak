package io.onebyone.authentication.it;

import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth/test")
public class AuthResource {

    @Inject
    SecurityIdentity identity;

    @RolesAllowed("view_profile")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> index() {

        return Uni.createFrom().item(
                Response.ok(identity.getPrincipal()).build()
        );
    }
}
