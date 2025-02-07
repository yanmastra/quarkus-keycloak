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
package io.onebyone.authentication.it;

import io.onebyone.authentication.payload.RefreshTokenPayload;
import io.onebyone.authentication.payload.UserTokenPayload;
import io.onebyone.authentication.security.AuthenticationService;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.jwt.util.KeyUtils;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.security.PrivateKey;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Path("/authentication")
@ApplicationScoped
public class AuthenticationResource {
    // add some rest methods here
    @Inject
    Logger logger;
    @Inject
    SecurityIdentity securityIdentity;
    @Inject
    AuthenticationService authenticationService;


    class MyUserTokenPayload implements UserTokenPayload{
        public String id;
        public String username;
        public String email;
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
    public Response hello() {
//        JwtClaimsBuilder claims = Jwt.claims()
//                .upn("yanmastra")
//                .preferredUserName("yanmastra")
//                .subject(UUID.randomUUID().toString())
//                .claim(Claims.email, "yanmastra61@gmail.com")
//                .claim(Claims.full_name, "Wayan Mastra")
//                .claim(sessionState, UUID.randomUUID().toString())
//                .claim("permissions", Json.createArrayBuilder()
//                        .add("view_profile")
//                        .add("manage_users")
//                        .add("manage_user_permission")
//                        .build()
//                ).issuer("http://localhost:4000")
//                .expiresIn(Duration.ofSeconds(30));

        payload = new MyUserTokenPayload();
        payload.id = UUID.randomUUID().toString();
        payload.username = "yanmastra";
        payload.email = "yanmastra61@gmail.com";
        payload.fullName = "Wayan Mastra";
        payload.permissions = Set.of(
                "view_profile",
                "manage_users",
                "manage_user_permission"
        );
        payload.attributes = Map.of(
                "jumpcloud_token", UUID.randomUUID().toString(),
                "tenant_access", Set.of("mjl", "mrb", "mdn")
        );
        return authenticationService.createAccessTokenResponse(payload);
    }

    @POST
    @Path("refresh")
    @Produces(MediaType.APPLICATION_JSON)
    public Response refresh(RefreshTokenPayload payload) {
        String sessionId = authenticationService.getSessionFromRefreshToken(payload.refreshToken);
        return authenticationService.createAccessTokenResponse(this.payload, sessionId, payload.refreshToken);
    }

    private PrivateKey getPrivateKey() {
        try {
            return KeyUtils.readPrivateKey("privatekey-2.pem");
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    @RolesAllowed({"VIEW_ALL"})
    @GET
    @Path("user")
    public Uni<Response> user() {
        logger.info("SecurityPrincipal:"+securityIdentity.getClass().getName());
        Principal principal = securityIdentity.getPrincipal();
        logger.info("Principal:"+principal.getClass().getName());
        return Uni.createFrom().item(Response.ok(principal).build());
    }

    @GET
    @Path("user_get_data")
    @RolesAllowed({"GET_DATA", "manage-users"})
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> user1(@Context SecurityContext context) {
        return Uni.createFrom().item(Response.ok(context.getUserPrincipal()).build());
    }

    @GET
    @Path("error_500")
    public Response internalError() {
        Integer integer = Integer.parseInt("2198h12687k");
        return Response.ok().build();
    }

    @GET
    @Path("error_400")
    public Response badRequestError() {
        try {
            throw new BadRequestException("Bad request");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
