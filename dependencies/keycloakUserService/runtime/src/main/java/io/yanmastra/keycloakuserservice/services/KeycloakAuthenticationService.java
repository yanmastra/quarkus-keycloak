package io.yanmastra.keycloakuserservice.services;

import io.yanmastra.keycloakuserservice.dto.Credentials;
import org.keycloak.representations.AccessTokenResponse;

/**
 * Interface for authentication services, defining methods for user login,
 * token refresh, and logout operations.
 */
public interface KeycloakAuthenticationService {
    /**
     * Logs in a user with the provided credentials.
     *
     * @param credentials the user's credentials containing username and password
     * @return AccessTokenResponse containing the access token if successful; null otherwise
     */
    AccessTokenResponse login(Credentials credentials);

    /**
     * Refreshes the access token using the provided refresh token.
     *
     * @param refreshToken the refresh token to use for getting a new access token
     * @return AccessTokenResponse containing the new access token if successful; null otherwise
     */
    AccessTokenResponse refreshAccessToken(String refreshToken);

    /**
     * Logs out the user by revoking the provided refresh token.
     *
     * @param refreshToken the refresh token to revoke
     * @throws Exception if an error occurs during the logout process
     */
    void logout(String refreshToken) throws Exception;
}