package io.onebyone.authentication.security;

import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtClaimsBuilder;
import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claims;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.onebyone.authentication.security.UserPrincipal.permissions;

@Singleton
public class AuthenticationService {

    @ConfigProperty(name = "smallrye.jwt.new-token.issuer", defaultValue = "-")
    String issuer;
    @Inject
    RoutingContext routingContext;

    public static final String accessToken = "access_token";
    public static final String data = "data";
    public static final String expiredAt = "expired_at";

    private final Map<String, String> sessionStates = new HashMap<>();

    public Map<String, Object> createAccessToken(UserTokenPayload userTokenPayload) {
        String sessionId = UUID.randomUUID().toString();
        sessionStates.put(sessionId, userTokenPayload.getId());
        Instant expiredAt = Instant.now().plus(Duration.ofDays(7));

        JwtClaimsBuilder jwtClaimsBuilder = Jwt.claims()
                .preferredUserName(userTokenPayload.getUsername())
                .claim(Claims.email, userTokenPayload.getEmail())
                .claim(Claims.full_name, userTokenPayload.getFullName())
                .subject(userTokenPayload.getId())
                .issuer(getIssuer())
                .claim(sessionId, sessionStates.get(sessionId))
                .claim(permissions, userTokenPayload.getPermission())
                .expiresAt(expiredAt);

        if (userTokenPayload.getAttributes() != null && !userTokenPayload.getAttributes().isEmpty()) {
            for (String key : userTokenPayload.getAttributes().keySet()) {
                jwtClaimsBuilder.claim(key, userTokenPayload.getAttributes().get(key));
            }
        }

        String accessToken = jwtClaimsBuilder.jws().keyId(sessionId).sign();
        Map<String, Object> response =  new HashMap<>(Map.of(
                AuthenticationService.accessToken, accessToken,
                data, userTokenPayload
        ));
        response.put(AuthenticationService.expiredAt, expiredAt.atZone(ZoneId.of("UTC")));
        return response;
    }

    public Response createAccessTokenResponse(UserTokenPayload userTokenPayload) {
        return Response.ok(createAccessToken(userTokenPayload)).build();
    }

    private String getIssuer() {
        if (StringUtils.isBlank(issuer) || "-".equals(issuer)) {
            return routingContext.request().absoluteURI();
        }
        return issuer;
    }

    public boolean checkSession(String sessionId) {
        return sessionStates.containsKey(sessionId);
    }

    public void removeSession(String sessionId) {
        sessionStates.remove(sessionId);
    }
}
