package io.yanmastra.keycloakuserservice.data;

import io.yanmastra.keycloakuserservice.dto.KeycloakAccountRepresentation;
import io.yanmastra.keycloakuserservice.dto.RealmMappingsRepresentation;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

@Produces("application/json")
public interface KeycloakClient {

    @POST
    @Path("/realms/{realm}/protocol/openid-connect/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    AccessTokenResponse login(
            @PathParam("realm") String realm,
            @FormParam("grant_type") String grantType,
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret,
            @FormParam("username") String username,
            @FormParam("password") String password);

    @POST
    @Path("/realms/{realm}/protocol/openid-connect/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    AccessTokenResponse refreshAccessToken(@PathParam("realm") String realm,
            @FormParam("grant_type") String grantType,
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret,
            @FormParam("refresh_token") String refreshToken);

    @POST
    @Path("/realms/{realm}/protocol/openid-connect/logout")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    void logout(@PathParam("realm") String realm,
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret,
            @FormParam("refresh_token") String refreshToken);

    @GET
    @Path("/admin/realms/{realm}/groups/{groupId}/role-mappings")
    @Consumes(MediaType.APPLICATION_JSON)
    RealmMappingsRepresentation getRealmMappings(
            @HeaderParam(HttpHeaders.AUTHORIZATION) String bearerAccessToken,
            @PathParam("realm") String realm,
            @PathParam("groupId") String groupId
    );

    @POST
    @Path("/admin/realms/{realm}/groups/{groupId}/role-mappings/realm")
    @Consumes(MediaType.APPLICATION_JSON)
    void postRealmMappings(
            @HeaderParam(HttpHeaders.AUTHORIZATION) String bearerAccessToken,
            @PathParam("realm") String realm,
            @PathParam("groupId") String groupId,
            List<RoleRepresentation> roles
    );

    @POST
    @Path("/admin/realms/{realm}/users")
    @Consumes(MediaType.APPLICATION_JSON)
    Response postUser(
            @HeaderParam(HttpHeaders.AUTHORIZATION) String bearerAccessToken,
            @PathParam("realm") String realm,
            UserRepresentation userRepresentation
    );

    @POST
    @Path("/realms/{realm}/protocol/openid-connect/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    Response impersonate(
            @PathParam("realm") String realm,
            @FormParam("grant_type") String grantType,
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret,
            @FormParam("subject_token") String subjectToken,
            @FormParam("requested_subject") String requestSubject
    );

    @GET
    @Path("/realms/{realm}/account")
    @Consumes(MediaType.APPLICATION_JSON)
    KeycloakAccountRepresentation getSelfAccount(
            @PathParam("realm") String realm,
            @HeaderParam(HttpHeaders.AUTHORIZATION) String bearerAccessToken
    );
}