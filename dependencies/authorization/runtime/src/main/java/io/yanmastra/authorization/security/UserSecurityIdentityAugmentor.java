package io.yanmastra.authorization.security;

import io.quarkus.oidc.runtime.OidcJwtCallerPrincipal;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserSecurityIdentityAugmentor implements SecurityIdentityAugmentor {

    @Override
    public Uni<SecurityIdentity> augment(SecurityIdentity securityIdentity, AuthenticationRequestContext authenticationRequestContext) {

        if (securityIdentity.getPrincipal() instanceof OidcJwtCallerPrincipal oidcJwtCallerPrincipal) {
            UserPrincipal principal = UserPrincipal.from(oidcJwtCallerPrincipal);
            return Uni.createFrom().item(new UserSecurityIdentity(securityIdentity, principal));
        }
        return Uni.createFrom().item(securityIdentity);
    }
}