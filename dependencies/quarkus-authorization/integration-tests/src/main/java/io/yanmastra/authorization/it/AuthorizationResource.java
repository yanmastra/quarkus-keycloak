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
package io.yanmastra.authorization.it;

import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.jboss.logging.Logger;

import java.security.Principal;

@Path("/authorization")
@ApplicationScoped
public class AuthorizationResource {
    // add some rest methods here

    @Inject
    Logger logger;

    @Inject
    SecurityIdentity securityIdentity;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String hello() {
        return Json.createObjectBuilder()
                .add("message", "Hello world!")
                .build()
                .toString();
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
}
