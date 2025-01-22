package io.yanmastra.authorization.security;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.oidc.runtime.OidcJwtCallerPrincipal;
import io.quarkus.security.credential.Credential;
import io.quarkus.security.credential.TokenCredential;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import org.jboss.logging.Logger;
import org.jose4j.jwt.JwtClaims;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIncludeProperties(value = {"credential", "id", "username", "email", "profile_name", "authorities", "company_access"})
public class UserPrincipal extends OidcJwtCallerPrincipal implements Credential, Serializable {
    private static final Logger log = Logger.getLogger(UserPrincipal.class);
    private static final String sub = "sub";
    private static final String preferredClaim = "preferred_username";
    private Set<String> authorities = null;

    public UserPrincipal(JwtClaims claims, TokenCredential credential) {
        super(claims, credential);
    }

    public UserPrincipal(JwtClaims claims, TokenCredential credential, String principalClaim) {
        super(claims, credential, principalClaim);
    }

    @JsonProperty("id")
    public String getUserId() {
        return super.getClaims().getClaimValueAsString(sub);
    }

    @JsonProperty("username")
    public String getUsername() {
        return getName();
    }

    @JsonProperty("name")
    public String getName() {
        return super.getClaims().getClaimValueAsString(preferredClaim);
    }

    @JsonProperty("email")
    public String getEmail() {
        return super.getClaims().getClaimValueAsString("email");
    }

    @JsonProperty("profile_name")
    public String getProfileName() {
        return super.getClaims().getClaimValueAsString("name");
    }

    @JsonProperty("authorities")
    public Set<String> getAuthorities() {
        if (authorities != null) return authorities;

        authorities = new HashSet<>(super.getGroups());
        Map<String, Object> claimMaps = super.getClaims().getClaimsMap();

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

    @JsonProperty("company_access")
    public Set<String> companyAccess() {
        try {
            JsonArray value = (JsonArray) super.getClaims().getClaimValue("company_access");
            return value == null ? Set.of() : new HashSet<>(value.getValuesAs(jsonValue -> ((JsonString) jsonValue).getString()));
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return Set.of();
        }
    }

    public String getSessionState() {
        return super.getClaims().getClaimValueAsString("session_state");
    }

    public static UserPrincipal from(OidcJwtCallerPrincipal principal) {
        return new UserPrincipal(principal.getClaims(), principal.getCredential());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof UserPrincipal upObj)) return false;
        return getUserId().equals(upObj.getUserId()) && getSessionState().equals(upObj.getSessionState());
    }
}
