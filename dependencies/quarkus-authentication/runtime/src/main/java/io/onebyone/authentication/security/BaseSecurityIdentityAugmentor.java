package io.onebyone.authentication.security;

import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BaseSecurityIdentityAugmentor implements SecurityIdentityAugmentor {

    @Override
    public Uni<SecurityIdentity> augment(SecurityIdentity securityIdentity, AuthenticationRequestContext authenticationRequestContext) {
        if (securityIdentity.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return Uni.createFrom().item(new UserSecurityIdentity(securityIdentity, userPrincipal));
        }
        return Uni.createFrom().item(securityIdentity);
    }
}