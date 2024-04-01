package com.acme.authorization.security;

import io.quarkus.oidc.runtime.OidcJwtCallerPrincipal;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class UserSecurityIdentityAugmentor implements SecurityIdentityAugmentor {

    @Inject
    Logger log;

    @Override
    public Uni<SecurityIdentity> augment(SecurityIdentity securityIdentity, AuthenticationRequestContext authenticationRequestContext) {
        log.info("augment("+securityIdentity.getClass().getName()+", "+authenticationRequestContext.getClass().getName()+"), principal:"+securityIdentity.getPrincipal().getName());

        if (securityIdentity.getPrincipal() instanceof OidcJwtCallerPrincipal oidcJwtCallerPrincipal) {
            UserPrincipal principal = UserPrincipal.from(oidcJwtCallerPrincipal);
            return Uni.createFrom().item(new UserSecurityIdentity(securityIdentity, principal));
        }
        return Uni.createFrom().item(securityIdentity);
    }
}