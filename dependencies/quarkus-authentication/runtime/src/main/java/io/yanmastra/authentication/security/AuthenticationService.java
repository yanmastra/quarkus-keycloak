package io.yanmastra.authentication.security;

import io.yanmastra.authentication.payload.UserTokenPayload;
import io.yanmastra.authentication.utils.KeyValueCacheUtils;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtClaimsBuilder;
import io.smallrye.jwt.util.KeyUtils;
import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claims;
import org.jboss.logging.Logger;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

import javax.crypto.SecretKey;
import java.net.URI;
import java.security.PublicKey;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.yanmastra.authentication.security.Constant.*;
import static io.yanmastra.authentication.security.UserPrincipal.permissions;

@Singleton
public class AuthenticationService {

    @ConfigProperty(name = PROP_SMALLRYE_JWT_ISSUER, defaultValue = "-")
    String issuer;
    @ConfigProperty(name = PROP_MP_JWT_VERIFY_CLOCK_SKEW, defaultValue = "0")
    int expiration;
    @ConfigProperty(name = PROP_SECURITY_TOKEN_ENCRYPTION_SECRET, defaultValue = "-")
    String tokenEncryptionSecret;
    @ConfigProperty(name = PROP_MP_PUBLIC_KEY_LOCATION, defaultValue = "-")
    String publicKeyLocation;
    @ConfigProperty(name = PROP_ALLOWED_JWT_ISSUER, defaultValue = "*")
    String allowedJwtIssuer;
    @ConfigProperty(name = PROP_SECURITY_IS_ENCRYPT_ACCESS_TOKEN, defaultValue = "false")
    String securityIsEncryptAccessToken;

    @Inject
    Logger log;

    @Inject
    RoutingContext routingContext;

    public static final String accessToken = "access_token";
    public static final String refreshToken = "refresh_token";
    public static final String data = "data";
    public static final String expiredAt = "expired_at";
    public static final String sessionStorage = "session_storage";

    private final Map<String, String> sessionStates = new HashMap<>();

    private JwtConsumer jwtConsumer = null;
    private JwtConsumer getJwtConsumer() {
        if (jwtConsumer == null) {
            if (StringUtils.isBlank(publicKeyLocation) || "-".equals(publicKeyLocation)) throw new RuntimeException("Missing required property " + PROP_MP_PUBLIC_KEY_LOCATION);
            try {
                if (StringUtils.isBlank(tokenEncryptionSecret) || "-".equals(tokenEncryptionSecret)) {
                    throw new RuntimeException("Missing required property " + PROP_SECURITY_TOKEN_ENCRYPTION_SECRET);
                }

                SecretKey secretKey = KeyUtils.createSecretKeyFromSecret(tokenEncryptionSecret);
                PublicKey publicKey = KeyUtils.readPublicKey(publicKeyLocation);

                JwtConsumerBuilder jcb = new JwtConsumerBuilder()
                        .setRequireExpirationTime()
                        .setVerificationKey(publicKey)
                        .setDecryptionKey(secretKey)
                        .setRelaxVerificationKeyValidation();

                jcb = getJwtConsumerBuilder(jcb, allowedJwtIssuer);
                jwtConsumer = jcb.build();
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        return jwtConsumer;
    }


    public Map<String, Object> createAccessToken(UserTokenPayload userTokenPayload) {
        return createAccessToken(userTokenPayload, null, null);
    }

    public Map<String, Object> createAccessToken(UserTokenPayload userTokenPayload, String sessionId, String refreshToken) {
        return createAccessToken(userTokenPayload, null, sessionId, refreshToken);
    }

    public Map<String, Object> createAccessToken(UserTokenPayload userTokenPayload, Instant expiredAt, String sessionId, String existingRefreshToken) {
        if (StringUtils.isBlank(tokenEncryptionSecret) || "-".equals(tokenEncryptionSecret)) throw new BadRequestException("Please set property \"" + PROP_SECURITY_TOKEN_ENCRYPTION_SECRET + "\".");
        if (StringUtils.isBlank(sessionId)) sessionId = UUID.randomUUID().toString();
        sessionStates.put(sessionId, userTokenPayload.getId());
        KeyValueCacheUtils.saveCache(sessionStorage, sessionId, userTokenPayload.getId());

        if (expiredAt == null) expiredAt = expiration == 0 ?
                Instant.now().plus(Duration.ofMinutes(30)) :
                Instant.now().plus(Duration.ofSeconds(expiration));

        String accessToken;
        if (Boolean.parseBoolean(securityIsEncryptAccessToken)) {
            accessToken = createClaimsForAccess(userTokenPayload, expiredAt).jws().keyId(sessionId).innerSign().encryptWithSecret(tokenEncryptionSecret);
        } else {
            accessToken = createClaimsForAccess(userTokenPayload, expiredAt).jws().keyId(sessionId).sign();
        }

        String refreshToken = StringUtils.isBlank(existingRefreshToken) ? createClaimsForRefresh(sessionId, Instant.now().plus(Duration.ofDays(7)))
                .jws()
                .innerSign().encryptWithSecret(tokenEncryptionSecret) : existingRefreshToken;

        Map<String, Object> response =  new HashMap<>(Map.of(
                AuthenticationService.accessToken, accessToken,
                AuthenticationService.refreshToken, refreshToken,
                data, userTokenPayload
        ));
        response.put(AuthenticationService.expiredAt, expiredAt.atZone(ZoneId.of("UTC")));
        return response;
    }

    private JwtClaimsBuilder createClaimsForAccess(UserTokenPayload userTokenPayload, Instant expiredAt) {
        JwtClaimsBuilder jwtClaimsBuilder = Jwt.claims()
                .preferredUserName(userTokenPayload.getUsername())
                .claim(Claims.email, userTokenPayload.getEmail())
                .claim(Claims.full_name, userTokenPayload.getFullName())
                .subject(userTokenPayload.getId())
                .issuer(getIssuer())
                .claim(permissions, userTokenPayload.getPermission())
                .expiresAt(expiredAt);

        if (userTokenPayload.getAttributes() != null && !userTokenPayload.getAttributes().isEmpty()) {
            for (String key : userTokenPayload.getAttributes().keySet()) {
                jwtClaimsBuilder.claim(key, userTokenPayload.getAttributes().get(key));
            }
        }

        return jwtClaimsBuilder;
    }

    private JwtClaimsBuilder createClaimsForRefresh(String sessionId, Instant expiredAt) {
        return Jwt.claims().subject(sessionId)
                .issuer(getIssuer())
                .expiresAt(expiredAt);
    }

    public Response createAccessTokenResponse(UserTokenPayload userTokenPayload) {
        return Response.ok(createAccessToken(userTokenPayload)).build();
    }

    public Response createAccessTokenResponse(UserTokenPayload userTokenPayload, String sessionId) {
        return Response.ok(createAccessToken(userTokenPayload, null, sessionId)).build();
    }

    public Response createAccessTokenResponse(UserTokenPayload userTokenPayload, String sessionId, String existingRefreshToken) {
        return Response.ok(createAccessToken(userTokenPayload, null, sessionId, existingRefreshToken)).build();
    }

    private String getIssuer() {
        if (StringUtils.isBlank(issuer) || "-".equals(issuer)) {
            URI uri = URI.create(routingContext.request().absoluteURI());
            return uri.getScheme() + "://" + uri.getAuthority();
        }
        return issuer;
    }

    public boolean checkSession(String sessionId, String userId) {
        if (StringUtils.isBlank(userId)) throw new BadRequestException("Invalid user id");
        return userId.equals(sessionStates.get(sessionId));
    }

    public void removeSession(String sessionId) {
        sessionStates.remove(sessionId);
    }

    public String getSessionFromRefreshToken(String refreshToken) {
        if (StringUtils.isBlank(tokenEncryptionSecret)) throw new BadRequestException("Please set property \"" + PROP_SECURITY_TOKEN_ENCRYPTION_SECRET + "\".");
        if (StringUtils.isBlank(refreshToken)) throw new BadRequestException("Invalid refresh token");

        String sessionId = null;
        try {
            JwtClaims claims = getJwtConsumer().processToClaims(refreshToken);
            sessionId = claims.getSubject();
        } catch (InvalidJwtException e) {
            throw new BadRequestException(e.getMessage(), e);
        } catch (MalformedClaimException e) {
            throw new RuntimeException(e);
        }

        if (StringUtils.isBlank(sessionId) &&
                !sessionStates.containsKey(sessionId) &&
                StringUtils.isBlank(KeyValueCacheUtils.findCache(sessionStorage, sessionId))
        ) throw new BadRequestException("Invalid session id");
        return sessionId;
    }

    public String getUserId(String sessionId) {
        String userId = sessionStates.get(sessionId);
        if (StringUtils.isBlank(userId)) userId = KeyValueCacheUtils.findCache(sessionStorage, sessionId);
        return userId;
    }

    public static JwtConsumerBuilder getJwtConsumerBuilder(JwtConsumerBuilder jcb, String allowedJwtIssuer) {
        if (StringUtils.isNotBlank(allowedJwtIssuer) && !"*".equals(allowedJwtIssuer)) {
            String[] issuers = allowedJwtIssuer.split(",");
            if (issuers.length > 1) {
                jcb = jcb.setExpectedIssuers(true, issuers);
            } else {
                jcb = jcb.setExpectedIssuer(true, allowedJwtIssuer);
            }
        }
        return jcb;
    }

    public void logout(String sessionId) {
        sessionStates.remove(sessionId);
        KeyValueCacheUtils.removeCache(sessionStorage, sessionId);
    }
}
