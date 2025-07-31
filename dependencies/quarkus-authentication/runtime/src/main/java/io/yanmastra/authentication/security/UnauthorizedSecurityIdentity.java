package io.yanmastra.authentication.security;

import io.quarkus.security.credential.Credential;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;

import java.security.Permission;
import java.security.Principal;
import java.util.Map;
import java.util.Set;

public class UnauthorizedSecurityIdentity implements SecurityIdentity {
    public static final UnauthorizedSecurityIdentity INSTANCE = new UnauthorizedSecurityIdentity();

    private Principal principal = null;
    @Override
    public Principal getPrincipal() {
        if (principal != null) return principal;

        principal = () -> "Unknown";
        return principal;
    }

    @Override
    public boolean isAnonymous() {
        return true;
    }

    @Override
    public Set<String> getRoles() {
        return Set.of();
    }

    @Override
    public boolean hasRole(String s) {
        return false;
    }

    @Override
    public <T extends Credential> T getCredential(Class<T> aClass) {
        return null;
    }

    @Override
    public Set<Credential> getCredentials() {
        return Set.of();
    }

    @Override
    public <T> T getAttribute(String s) {
        return null;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of();
    }

    @Override
    public Uni<Boolean> checkPermission(Permission permission) {
        return Uni.createFrom().item(false);
    }
}
