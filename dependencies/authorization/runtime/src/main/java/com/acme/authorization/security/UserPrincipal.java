package com.acme.authorization.security;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.oidc.runtime.OidcJwtCallerPrincipal;
import io.quarkus.security.credential.Credential;
import io.quarkus.security.credential.TokenCredential;
import jakarta.json.JsonArray;
import jakarta.json.JsonString;
import org.jboss.logging.Logger;
import org.jose4j.jwt.JwtClaims;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIncludeProperties(value = {"credential", "id", "username", "email", "name", "authorities", "company_access"})
public class UserPrincipal extends OidcJwtCallerPrincipal implements Credential, Serializable {
    private static final Logger log = Logger.getLogger(UserPrincipal.class);

    public UserPrincipal(JwtClaims claims, TokenCredential credential) {
        super(claims, credential);
    }

    public UserPrincipal(JwtClaims claims, TokenCredential credential, String principalClaim) {
        super(claims, credential, principalClaim);
    }

    @JsonProperty("id")
    public String getUserId() {
        return super.getClaims().getClaimValueAsString("sub");
    }

    @JsonProperty("username")
    public String getUsername() {
        return super.getClaims().getClaimValueAsString("preferred_username");
    }

    @JsonProperty("email")
    public String getEmail() {
        return super.getClaims().getClaimValueAsString("email");
    }

    @JsonProperty("name")
    public String getName() {
        return super.getClaims().getClaimValueAsString("name");
    }

    @JsonProperty("authorities")
    public Set<String> getAuthorities() {
        return super.getGroups();
    }

    @JsonProperty("company_access")
    public Set<String> companyAccess() {
        try {
            JsonArray value = (JsonArray) super.getClaims().getClaimValue("company_access");
            return new HashSet<>(value.getValuesAs(jsonValue -> ((JsonString) jsonValue).getString()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new HashSet<>();
        }
    }

    public static UserPrincipal from(OidcJwtCallerPrincipal principal) {
        return new UserPrincipal(principal.getClaims(), principal.getCredential());
    }
}
