package io.onebyone.authorization.security;

import io.quarkus.security.credential.Credential;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import org.apache.commons.lang3.StringUtils;

import java.security.Permission;
import java.security.Principal;
import java.util.Map;
import java.util.Set;

public class UserSecurityIdentity implements SecurityIdentity {
    private final SecurityIdentity securityIdentity;
    private final UserPrincipal principal;

    public UserSecurityIdentity(SecurityIdentity securityIdentity, UserPrincipal principal) {
        this.securityIdentity = securityIdentity;
        this.principal = principal;
    }

    @Override
    public Principal getPrincipal() {
        return principal;
    }

    @Override
    public boolean isAnonymous() {
        return StringUtils.isBlank(principal.getEmail());
    }

    @Override
    public Set<String> getRoles() {
        return principal.getAuthorities();
    }

    @Override
    public boolean hasRole(String s) {
        return principal.getAuthorities().contains(s);
    }

    @Override
    public <T extends Credential> T getCredential(Class<T> aClass) {
        return securityIdentity.getCredential(aClass);
    }

    @Override
    public Set<Credential> getCredentials() {
        return Set.of(principal.getCredential());
    }

    @Override
    public <T> T getAttribute(String s) {
        return securityIdentity.getAttribute(s);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return securityIdentity.getAttributes();
    }

    @Override
    public Uni<Boolean> checkPermission(Permission permission) {
        return securityIdentity.checkPermission(permission);
    }
}
