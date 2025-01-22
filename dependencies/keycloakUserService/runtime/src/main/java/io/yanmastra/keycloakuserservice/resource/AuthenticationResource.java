package io.yanmastra.keycloakuserservice.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.security.Authenticated;
import io.quarkus.security.UnauthorizedException;
import io.yanmastra.keycloakuserservice.dto.Credentials;
import io.yanmastra.keycloakuserservice.dto.RefreshTokenRequest;
import io.yanmastra.keycloakuserservice.dto.UserDto;
import io.yanmastra.keycloakuserservice.services.UserService;
import io.yanmastra.keycloakuserservice.services.impl.KeycloakAuthenticationServiceImpl;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.representations.AccessTokenResponse;

import java.util.Map;

/**
 * AuthenticationResource handles authentication-related requests.
 * It provides endpoints for login, token refresh, and logout.
 */
@Consumes(MediaType.APPLICATION_JSON)
@Path("/api/auth")
@PermitAll
public class AuthenticationResource {
    @Inject
    KeycloakAuthenticationServiceImpl authService; // Injected service for Keycloak authentication
    @Inject
    UserService userService;
    @Inject
    ObjectMapper objectMapper;
    @Inject
    Logger log;

    /**
     * Endpoint for user login.
     *
     * @param credentials the user's credentials (username and password)
     * @return a Response containing the access token or an unauthorized error
     *         message
     */
    @POST
    @Path("/login")
    public Response login(Credentials credentials) {
        try {
            // Attempt to log in and retrieve an access token
            log.debug("credential: "+objectMapper.writeValueAsString(credentials));
            AccessTokenResponse tokenResponse = authService.login(credentials);
            if (tokenResponse != null) {
                UserDto user = userService.getUserByToken(tokenResponse.getToken());
                if (user == null)
                    throw new NotFoundException("User not found");
                return Response.ok(Map.of("access", tokenResponse, "data", user)).build();
            } else {
                throw new UnauthorizedException("Invalid credentials");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new UnauthorizedException("Invalid credentials");
        }
    }

    /**
     * Endpoint for refreshing an access token using a refresh token.
     *
     * @param refreshTokenRequest the request containing the refresh token
     * @return a Response containing the new access token or an error message
     */
    @POST
    @Path("/refresh")
    @Produces(MediaType.APPLICATION_JSON)
    @Authenticated
    public Response refresh(RefreshTokenRequest refreshTokenRequest) {
        try {
            // Use the refresh token from the request payload
            String refreshToken = refreshTokenRequest.getRefreshToken();

            // Attempt to refresh the access token
            AccessTokenResponse newTokenResponse = authService.refreshAccessToken(refreshToken);

            if (newTokenResponse != null) {
                return Response.ok(newTokenResponse).build(); // Respond with the new access token
            } else {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Refresh token is invalid or expired.")
                        .build(); // Respond with an unauthorized error
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Token refresh failed: " + e.getMessage())
                    .build(); // Respond with an error message
        }
    }

    /**
     * Endpoint for user logout.
     *
     * @param refreshTokenRequest the request containing the refresh token
     * @return a Response indicating success or an error message
     */
    @POST
    @Path("/logout")
    @Authenticated
    public Response logout(RefreshTokenRequest refreshTokenRequest) {
        try {
            // Call the logout method in the authentication service
            authService.logout(refreshTokenRequest.getRefreshToken());
            return Response.noContent().build(); // Respond with a success message
        } catch (UnauthorizedException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(e.getMessage())
                    .build(); // Respond with an unauthorized error
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to logout. Please try again later.")
                    .build(); // Respond with an error message
        }
    }
}
