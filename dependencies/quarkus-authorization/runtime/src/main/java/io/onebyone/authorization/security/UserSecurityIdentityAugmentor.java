package io.onebyone.authorization.security;

import io.onebyone.authentication.security.UserPrincipal;
import io.onebyone.authentication.security.UserSecurityIdentity;
import io.quarkus.oidc.runtime.OidcJwtCallerPrincipal;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

public class UserSecurityIdentityAugmentor implements SecurityIdentityAugmentor {

    @Inject
    Logger log;

    @Override
    public Uni<SecurityIdentity> augment(SecurityIdentity securityIdentity, AuthenticationRequestContext authenticationRequestContext) {
        log.error("UserSecurityIdentityAugmentor:augment: " + securityIdentity);
        if (securityIdentity.getPrincipal() instanceof OidcJwtCallerPrincipal oidcJwtCallerPrincipal) {
            UserPrincipal principal = new UserPrincipal(oidcJwtCallerPrincipal.getClaims(), oidcJwtCallerPrincipal.getCredential());
            return Uni.createFrom().item(new UserSecurityIdentity(securityIdentity, principal));
        }
        return Uni.createFrom().item(securityIdentity);
    }
}