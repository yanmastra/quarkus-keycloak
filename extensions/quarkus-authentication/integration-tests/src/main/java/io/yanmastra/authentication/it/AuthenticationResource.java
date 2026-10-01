/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.yanmastra.authentication.it;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import io.yanmastra.authentication.payload.RefreshTokenPayload;
import io.yanmastra.authentication.payload.UserTokenPayload;
import io.yanmastra.authentication.security.AuthenticationService;
import io.yanmastra.authentication.service.SecurityLifeCycleService;
import io.yanmastra.authentication.utils.CookieSessionUtils;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.logging.Logger;

import java.security.Principal;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Path("/authentication")
public class AuthenticationResource {
    private static final Log log = LogFactory.getLog(AuthenticationResource.class);
    // add some rest methods here
    @Inject
    Logger logger;
    @Inject
    SecurityIdentity securityIdentity;
    @Inject
    AuthenticationService authenticationService;
    @Inject
    SecurityLifeCycleService securityLifeCycleService;


    public static class MyUserTokenPayload implements UserTokenPayload{
        public String id;
        public String username;
        public String email;
        @JsonProperty("full_name")
        public String fullName;
        public Set<String> permissions;
        public Map<String, Object> attributes;

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public String getEmail() {
            return email;
        }

        @Override
        public String getFullName() {
            return fullName;
        }

        @Override
        public Set<String> getPermission() {
            return permissions;
        }

        @Override
        public Map<String, Object> getAttributes() {
            return attributes;
        }
    }

    private MyUserTokenPayload payload = null;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getToken() {

        String sessionId = UUID.randomUUID().toString();
        payload = (MyUserTokenPayload) securityLifeCycleService.onCreateAccessTokenPayload(sessionId);

        Map<String, Object> newAccess = authenticationService.createAccessToken(payload);
        NewCookie cookie = CookieSessionUtils.createSessionCookie(newAccess);
        return Response.ok(newAccess)
                .cookie(cookie)
                .build();
    }

    @POST
    @Path("refresh")
    @Produces(MediaType.APPLICATION_JSON)
    public Response refresh(RefreshTokenPayload payload) {
        return authenticationService.createAccessTokenResponse(payload.refreshToken);
    }

    // --- reactive endpoints: these run on the event loop, so they must use the async service methods ---

    @GET
    @Path("async")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getTokenAsync() {
        MyUserTokenPayload tokenPayload = (MyUserTokenPayload) securityLifeCycleService.onCreateAccessTokenPayload(UUID.randomUUID().toString());
        return authenticationService.createAccessTokenAsync(tokenPayload)
                .map(tokens -> Response.ok(tokens).cookie(CookieSessionUtils.createSessionCookie(tokens)).build());
    }

    @POST
    @Path("async/refresh")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> refreshAsync(RefreshTokenPayload payload) {
        return authenticationService.createAccessTokenAsync(payload.refreshToken)
                .map(tokens -> Response.ok(tokens).build());
    }

    @POST
    @Path("async/session")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<Response> sessionUserAsync(RefreshTokenPayload payload) {
        return authenticationService.getUserIdAsync(sessionIdOf(payload))
                .map(userId -> userId == null ? Response.noContent().build() : Response.ok(userId).build());
    }

    @POST
    @Path("async/session/check")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<Response> checkSessionAsync(RefreshTokenPayload payload, @QueryParam("userId") String userId) {
        return authenticationService.checkSessionAsync(sessionIdOf(payload), userId)
                .map(matches -> Response.ok(String.valueOf(matches)).build());
    }

    @POST
    @Path("async/logout")
    public Uni<Response> logoutAsync(RefreshTokenPayload payload) {
        return authenticationService.logoutAsync(sessionIdOf(payload))
                .map(ignored -> Response.noContent().build());
    }

    private String sessionIdOf(RefreshTokenPayload payload) {
        try {
            return authenticationService.getRefreshTokenClaims(payload.refreshToken).getSubject();
        } catch (org.jose4j.jwt.MalformedClaimException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    @RolesAllowed({"VIEW_ALL"})
    @GET
    @Path("user")
    public Uni<Response> user() {
        Principal principal = securityIdentity.getPrincipal();
        return Uni.createFrom().item(Response.ok(principal).build());
    }

    @GET
    @Path("user_get_data")
    @RolesAllowed({"GET_DATA", "manage_users"})
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> user1(@Context ContainerRequestContext context) {
        for (String key: context.getCookies().keySet()) {
            log.debug("key:"+key+", value:"+context.getCookies().get(key));
        }
        return Uni.createFrom().item(Response.ok(context.getSecurityContext().getUserPrincipal()).build());
    }

    @GET
    @Path("forbidden")
    @RolesAllowed({"GET_DATA"})
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getForbidden(@Context ContainerRequestContext context) {
        for (String key: context.getCookies().keySet()) {
            log.debug("key:"+key+", value:"+context.getCookies().get(key));
        }
        return Uni.createFrom().item(Response.ok(context.getSecurityContext().getUserPrincipal()).build());
    }

    @GET
    @Path("permissions")
    @RolesAllowed({"VIEW_ALL"})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPermissions() {
        Set<String> names = securityIdentity.getPermissions().stream()
                .map(java.security.Permission::getName)
                .collect(java.util.stream.Collectors.toSet());
        return Response.ok(names).build();
    }
}
