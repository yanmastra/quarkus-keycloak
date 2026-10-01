package io.yanmastra.quarkus.rediscache.it;

import io.smallrye.mutiny.Uni;
import io.yanmastra.authentication.security.AuthenticationService;
import io.yanmastra.authentication.utils.CookieSessionUtils;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/session")
public class SessionResource {

    @Inject
    AuthenticationService authenticationService;

    /** Blocking endpoint: runs on a worker thread, so the blocking cache API is fine here. */
    @GET
    @Path("login")
    @Produces(MediaType.APPLICATION_JSON)
    public Response login() {
        Map<String, Object> tokens = authenticationService.createAccessToken(new TestUser());
        return Response.ok(tokens).cookie(CookieSessionUtils.createSessionCookie(tokens)).build();
    }

    /** Reactive endpoint: runs on the event loop, so it must use the async API. */
    @GET
    @Path("login-async")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> loginAsync() {
        return authenticationService.createAccessTokenAsync(new TestUser())
                .map(tokens -> Response.ok(tokens).cookie(CookieSessionUtils.createSessionCookie(tokens)).build());
    }

    @GET
    @Path("me")
    @RolesAllowed({"VIEW_ALL"})
    public Uni<Response> me() {
        return Uni.createFrom().item(Response.ok("authenticated").build());
    }
}
