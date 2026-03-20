package io.yanmastra.authentication.it;

import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;

@Path("/auth/test")
public class AuthResource {

    @Inject
    SecurityIdentity identity;

//    @RolesAllowed("view_profile")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> index() {

        return Uni.createFrom().item(
                Response.ok(identity.getPrincipal()).build()
        );
    }

    public static void main(String[] args) {
        String url = "http://localhost:4001/api/v2/auth/redirect?code=ory_ac_Uc2mxjvnTcRNFMRu3Q_3qCyXBwsXmszf15mN3uAkKXY.HGBeqINy7zdV7vD6SIck49qVeEWuOP5DVo6IO-7Q1oU&scope=openid&state=5f941882-a8cf-4246-a1ad-84aba2a09124";
        URI uri = URI.create(url);
        String baseUrl = uri.getScheme() + "://" + uri.getAuthority();
        System.out.println("authority: " + baseUrl);
    }
}
