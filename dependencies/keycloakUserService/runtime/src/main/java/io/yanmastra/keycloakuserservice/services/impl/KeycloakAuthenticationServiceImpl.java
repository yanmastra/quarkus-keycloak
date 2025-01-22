package io.yanmastra.keycloakuserservice.services.impl;

import io.yanmastra.keycloakuserservice.data.KeycloakAdminRepository;
import io.yanmastra.keycloakuserservice.data.KeycloakClient;
import io.yanmastra.keycloakuserservice.dto.Credentials;
import io.yanmastra.keycloakuserservice.services.KeycloakAuthenticationService;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.keycloak.OAuth2Constants;
import org.keycloak.representations.AccessTokenResponse;

/**
 * Implementation of the AuthenticationService for Keycloak authentication.
 */
@ApplicationScoped
public class KeycloakAuthenticationServiceImpl implements KeycloakAuthenticationService {
    @ConfigProperty(name = "keycloak_realm", defaultValue = "")
    String realm;

    @ConfigProperty(name = "quarkus.oidc.client-id", defaultValue = "backend")
    String clientId;

    @ConfigProperty(name = "quarkus.oidc.credentials.secret", defaultValue = "")
    String clientSecret;

    @Inject
    SecurityIdentity securityIdentity;
    @Inject
    KeycloakAdminRepository adminRepository;

    private static final Logger log = Logger.getLogger(KeycloakAuthenticationServiceImpl.class);

    private KeycloakClient keycloakClient;

    /**
     * Initializes the KeycloakClient after the configuration properties are
     * injected.
     */
    @PostConstruct
    public void init() {
        if (keycloakClient == null) {
            adminRepository.init();
            keycloakClient = adminRepository.getKeycloakClient();
        }
    }

    @Override
    public AccessTokenResponse login(Credentials credentials) {
        return keycloakClient.login(
                realm,
                OAuth2Constants.PASSWORD,
                clientId,
                clientSecret,
                credentials.getUsername(),
                credentials.getPassword()
        );
    }

    @Override
    public AccessTokenResponse refreshAccessToken(String refreshToken) {
        try {
            return keycloakClient.refreshAccessToken(realm,
                    OAuth2Constants.REFRESH_TOKEN,
                    clientId,
                    clientSecret,
                    refreshToken);
        } catch (Exception e) {
            log.error("Error refreshing access token", e);
            return null; // Consider throwing a custom exception in production code
        }
    }

    @Override
    public void logout(String refreshToken) throws Exception {
        try {
            keycloakClient.logout(realm, clientId, clientSecret, refreshToken);
            log.info("Successfully logged out (token revoked).");
        } catch (Exception e) {
            log.error("Error logging out", e);
            throw e; // Rethrow the exception for further handling
        }
    }

    /**
     * Get the current token username
     * 
     * @return the username of the current token
     * @throws Exception if an error occurs while getting the username
     */
    public String getCurrentTokenUsername() {
        return securityIdentity.getPrincipal().getName();
    }
}