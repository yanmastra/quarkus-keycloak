package io.yanmastra.authentication.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.security.credential.Credential;
import io.quarkus.security.credential.TokenCredential;
import io.smallrye.jwt.auth.principal.DefaultJWTCallerPrincipal;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import org.eclipse.microprofile.jwt.Claims;
import org.jboss.logging.Logger;
import org.jose4j.jwt.JwtClaims;

import java.io.Serializable;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.eclipse.microprofile.jwt.Claims.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIncludeProperties(value = {"id", "username", "name", "email", "profile_name", "current_tenant", "tenant_access", "authorities"})
public class UserPrincipal extends DefaultJWTCallerPrincipal implements Principal, Credential, Serializable {
    private static final Logger log = Logger.getLogger(UserPrincipal.class);
    public static final String tenantAccess = "tenant_access";
    public static final String currentTenant = "current_tenant";
    public static final String sessionState = "session_state";
    public static final String permissions = "permissions";
    private Set<String> authorities = null;
    private final JwtClaims claims;
    private final TokenCredential credential;
    private Map<String, Object> additionalClaims = null;

    public UserPrincipal(JwtClaims claims, TokenCredential credential) {
        super(credential.getType(), claims);
        this.claims = claims;
        this.credential = credential;
    }

    @JsonProperty("id")
    public String getUserId() {
        return claims.getClaimValueAsString(sub.name());
    }

    @JsonProperty("username")
    public String getUsername() {
        return claims.getClaimValueAsString(preferred_username.name());
    }

    public String getName() {
        return claims.getClaimValueAsString(preferred_username.name());
    }

    @JsonProperty("email")
    public String getEmail() {
        return claims.getClaimValueAsString(email.name());
    }

    @JsonProperty("profile_name")
    public String getProfileName() {
        return claims.getClaimValueAsString(full_name.name());
    }

    @JsonProperty(currentTenant)
    public String getCurrentTenant() {
        return claims.getClaimValueAsString(currentTenant);
    }

    @JsonProperty("authorities")
    public Set<String> getAuthorities() {
        if (authorities != null) return authorities;

        authorities = new HashSet<>(super.getGroups());
        Map<String, Object> claimMaps = claims.getClaimsMap();

        if (claimMaps.containsKey(permissions) && claimMaps.get(permissions) instanceof JsonArray permissions) {
            try {
                authorities.addAll(permissions.stream().map(v -> {
                    if (v instanceof JsonString jsonString) return jsonString.getString();
                    return null;
                }).filter(Objects::nonNull).collect(Collectors.toSet()));
            } catch (Exception e) {
                log.warn(e.getMessage(), new RuntimeException(e));
            }
        }

        if (claimMaps.containsKey("realm_access") && claimMaps.get("realm_access") instanceof JsonObject realmAccess && realmAccess.containsKey("roles")) {
            try {
                authorities.addAll(realmAccess.getJsonArray("roles").stream().map(v -> {
                    if (v instanceof JsonString sValue) return sValue.getString();
                    return null;
                }).filter(Objects::nonNull).collect(Collectors.toSet()));
            } catch (Exception e) {
                log.warn(e.getMessage(), new RuntimeException(e));
            }
        }

        if (claimMaps.containsKey("resource_access") && claimMaps.get("resource_access") instanceof JsonObject resAccess) {
            try {
                if (!resAccess.isEmpty()) {
                    for (String key: resAccess.keySet()) {
                        if (resAccess.get(key) instanceof JsonObject roleAccChild && roleAccChild.containsKey("roles")) {
                            authorities.add(key);
                            authorities.addAll(roleAccChild.getJsonArray("roles").stream().map(v -> {
                                if (v instanceof JsonString sValue) return sValue.getString();
                                return null;
                            }).filter(Objects::nonNull).collect(Collectors.toSet()));
                        }
                    }
                }
            } catch (Exception e) {
                log.warn(e.getMessage(), new RuntimeException(e));
            }
        }
        return authorities;
    }

    @JsonProperty("tenant_access")
    public Set<String> tenantAccess() {
        if (claims.hasClaim(tenantAccess) && claims.getClaimValue(tenantAccess) instanceof JsonArray tenantAccessValue) {
            return tenantAccessValue.stream().map(tenantCode -> {
                if (tenantCode instanceof JsonString tenantCodeString) return tenantCodeString.getString();
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toSet());
        }
        return Set.of();
    }

    @JsonIgnore
    public String getSessionState() {
        return claims.getClaimValueAsString(sessionState);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof UserPrincipal upObj)) return false;
        return getUserId().equals(upObj.getUserId()) && getSessionState().equals(upObj.getSessionState());
    }

    @JsonIgnore
    public TokenCredential getCredential() {
        return credential;
    }

    public Object getAdditionalClaim(String key) {
        if (additionalClaims == null) fetchAdditionalClaims();
        return additionalClaims.get(key);
    }

    private void fetchAdditionalClaims() {
        Map<String, Object> additionalClaimsMap = new HashMap<>();
        Set<String> keys = Stream.of(Claims.values()).map(Enum::name)
                .filter(name -> !UNKNOWN.name().equals(name))
                .collect(Collectors.toSet());

        for (String name: claims.getClaimsMap().keySet()) {
            if (!keys.contains(name)) {
                additionalClaimsMap.put(name, claims.getClaimValue(name));
            }
        }
        additionalClaims = Collections.unmodifiableMap(additionalClaimsMap);
    }
}
