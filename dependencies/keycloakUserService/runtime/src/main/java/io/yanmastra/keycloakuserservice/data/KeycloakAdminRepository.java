package io.yanmastra.keycloakuserservice.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.ext.web.handler.HttpException;
import io.yanmastra.keycloakuserservice.dto.KeycloakAccountRepresentation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.logging.Logger;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static io.yanmastra.keycloakuserservice.UserManagementConstant.BEARER_TOKEN;
import static io.yanmastra.keycloakuserservice.UserManagementConstant.COMPANY_ACCESS;
import static org.keycloak.OAuth2Constants.PASSWORD;

@ApplicationScoped
public class KeycloakAdminRepository {
    @ConfigProperty(name = "keycloak_realm")
    String realm;
    @ConfigProperty(name = "quarkus.keycloak.admin-client.server-url")
    String serverUrl;
    @ConfigProperty(name = "keycloak_service_username")
    String username;
    @ConfigProperty(name = "keycloak_service_password")
    String password;
    @ConfigProperty(name = "quarkus.oidc.client-id")
    String clientId;
    @ConfigProperty(name = "quarkus.oidc.credentials.secret")
    String secret;
    @Inject
    Logger logger;
    @Inject
    ObjectMapper objectMapper;


    private Keycloak keycloak;
    private KeycloakClient keycloakClient;

    /**
     * Initializes the KeycloakClient after the configuration properties are
     * injected.
     */
    public void init() {
        logger.debug("realm: " + realm+", serverUrl: " + serverUrl+", username: " + username+", password: " + password + ", clientId: " + clientId + ", secret: " + secret);
        logger.info("setting up keycloak client");

        if (keycloak == null)
            keycloak = KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm(realm)
                    .clientId(clientId)
                    .grantType(PASSWORD)
                    .username(username)
                    .password(password)
                    .clientSecret(secret)
                    .build();

        if (keycloakClient == null)
            keycloakClient = RestClientBuilder.newBuilder()
                    .baseUri(URI.create(serverUrl))
                    .build(KeycloakClient.class);
    }

    public KeycloakClient getKeycloakClient() {
        return keycloakClient;
    }

    public List<RoleRepresentation> getRoles(
            String search,
            Integer start,
            Integer limit
    ) {
        return keycloak.realm(realm).roles().list(search, start, limit);
    }

    public List<GroupRepresentation> getRoleGroups(
            String search,
            Integer start,
            Integer limit
    ) {
        return keycloak.realm(realm).groups().groups(search, start, limit);
    }

    public List<GroupRepresentation> getRoleGroupChildren(
            String groupId,
            String search,
            Integer start,
            Integer limit
    ) {
        return keycloak.realm(realm).groups().group(groupId).getSubGroups(search, false, start, limit, true);
    }

    public void updateGroup(GroupRepresentation representation) {
        keycloak.realm(realm).groups().group(representation.getId()).update(representation);
    }

    public String getAdminAccessToken() {
        return keycloak.tokenManager().getAccessTokenString();
    }

    public GroupRepresentation createGroup(String parentId, String name) {
        GroupRepresentation group = new GroupRepresentation();
        group.setName(name);
        try(Response response = keycloak.realm(realm).groups().group(parentId).subGroup(group)) {
            if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                return response.readEntity(GroupRepresentation.class);
            }
            throw new HttpException(response.getStatus(), response.getStatusInfo().getReasonPhrase()+" "+response.getStatusInfo().getStatusCode());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public String createUser(String email, String username, String firstName, String lastName, String companyId, String password, List<String> groupPaths) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(StringUtils.isBlank(username) ? email : username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setAttributes(Map.of(
                COMPANY_ACCESS, List.of(companyId)
        ));
        user.setEnabled(true);
        user.setEmailVerified(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        user.setCredentials(List.of(credential));
        user.setGroups(groupPaths);

        try {
            logger.debug("payload:" + objectMapper.writeValueAsString(user));
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
        }
        try (Response response = keycloak.realm(realm).users().create(user)) {
            if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                String location = response.getHeaderString(HttpHeaders.LOCATION);
                logger.debug("user created with location: " + location);
                return location.substring(location.lastIndexOf("/")+1);
            }
            logger.error("error: " + response.getStatus()+", "+response.getStatusInfo().getReasonPhrase());
            throw new HttpException(response.getStatus(), response.getStatusInfo().getReasonPhrase()+" "+response.getStatusInfo().getStatusCode());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void deleteUser(String id) {
        try(Response response = keycloak.realm(realm).users().delete(id)) {
            if (response.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {
                throw new HttpException(response.getStatus(), response.getStatusInfo().getReasonPhrase()+" "+response.getStatusInfo().getStatusCode());
            }
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            throw new HttpException(500, "Failed to delete user");
        }
    }

    public KeycloakAccountRepresentation getSelfAccount(String token) {
        logger.debug("getting self account: " + token);
        return keycloakClient.getSelfAccount(realm, BEARER_TOKEN + token);
    }

    public UserRepresentation getKeycloakUser(String userId) {
        return keycloak.realm(realm).users().get(userId).toRepresentation();
    }

    public void updateUser(String userId, UserRepresentation userRep) {
        keycloak.realm(realm).users().get(userId).update(userRep);
    }

    public List<UserRepresentation> getAllUsers() {
        return keycloak.realm(realm).users().list();
    }

    public List<GroupRepresentation> getUserGroup(String userId) {
        return keycloak.realm(realm).users().get(userId).groups();
    }
}
