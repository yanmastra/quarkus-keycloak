package io.yanmastra.authentication.security;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtClaimsBuilder;
import io.smallrye.jwt.util.KeyUtils;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.vertx.ext.web.RoutingContext;
import io.yanmastra.authentication.payload.UserTokenPayload;
import io.yanmastra.authentication.service.SecurityLifeCycleService;
import io.yanmastra.authentication.utils.CookieSessionUtils;
import io.yanmastra.quarkusBase.utils.KeyValueCacheUtils;
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
import static io.yanmastra.authentication.security.UserPrincipal.*;

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
    @ConfigProperty(name = PROP_COOKIE_TOKEN_ENABLED, defaultValue = "false")
    Boolean cookieTokenEnabled;

    @Inject
    Logger log;

    @Inject
    RoutingContext routingContext;

    public static final String keyAccessToken = "access_token";
    public static final String keyRefreshToken = "refresh_token";
    public static final String keyData = "data";
    public static final String keyExpiredAt = "expired_at";
    public static final String keySessionStorage = "session_storage";
    public static final String keyCookieToken = "cookie_token";

    private JwtConsumer jwtConsumer = null;
    private JwtConsumer getJwtConsumer() {
        if (jwtConsumer == null) {
            if (StringUtils.isBlank(publicKeyLocation) || "-".equals(publicKeyLocation)) throw new RuntimeException("Missing required property " + PROP_MP_PUBLIC_KEY_LOCATION);
            try {
                if (StringUtils.isBlank(tokenEncryptionSecret) || "-".equals(tokenEncryptionSecret)) {
                    throw new RuntimeException("Missing required property " + PROP_SECURITY_TOKEN_ENCRYPTION_SECRET);
                }

                SecretKey secretKey = KeyUtils.createSecretKeyFromSecret(tokenEncryptionSecret);
                PublicKey publicKey = Constant.getPublicKey(publicKeyLocation);

                JwtConsumerBuilder jcb = new JwtConsumerBuilder()
                        .setRequireExpirationTime()
                        .setVerificationKey(publicKey)
                        .setDecryptionKey(secretKey);

                jcb = getJwtConsumerBuilder(jcb, allowedJwtIssuer);
                jwtConsumer = jcb.build();
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        return jwtConsumer;
    }


    public Map<String, Object> createAccessToken(UserTokenPayload userTokenPayload) {
        return createAccessToken(userTokenPayload, null, null, null, null);
    }

    public Map<String, Object> createAccessToken(String refreshToken) {
        return createAccessToken(refreshToken, null);
    }

    public Map<String, Object> createAccessToken(String refreshToken, UserTokenPayload payload) {
        if (StringUtils.isBlank(refreshToken)) throw new BadRequestException("Missing refresh token!");

        String sessionId = null;
        String userId = null;
        String issuer = null;
        try {
            JwtClaims claims = getRefreshTokenClaims(refreshToken);
            sessionId = claims.getSubject();
            userId = getUserId(sessionId);
            issuer = claims.getIssuer();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        if (payload != null) {
            return createAccessToken(
                    payload,
                    expiration == 0 ?
                            Instant.now().plus(Duration.ofMinutes(30)) :
                            Instant.now().plus(Duration.ofSeconds(expiration)),
                    issuer, sessionId, refreshToken
            );
        }

        try (InstanceHandle<SecurityLifeCycleService> reAuthenticateServiceInstanceHandle = Arc.container().instance(SecurityLifeCycleService.class)) {
            if (!reAuthenticateServiceInstanceHandle.isAvailable()) throw new IllegalArgumentException("Please implement User Service");
            SecurityLifeCycleService securityLifeCycleService = reAuthenticateServiceInstanceHandle.get();
            payload = securityLifeCycleService.onCreateAccessTokenPayload(userId);
            return createAccessToken(
                    payload,
                    expiration == 0 ?
                            Instant.now().plus(Duration.ofMinutes(30)) :
                            Instant.now().plus(Duration.ofSeconds(expiration)),
                    issuer, sessionId, refreshToken
            );
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage(), e);
        }
    }

    public Uni<Map<String, Object>> createAccessTokenAsync(String refreshToken) {
        return createAccessTokenAsync(refreshToken, null);
    }

    /**
     * Non-blocking variant of {@link #createAccessToken(String, UserTokenPayload)}. When no payload is given,
     * {@code SecurityLifeCycleService.onCreateAccessTokenPayload} is invoked on a worker thread because it is user
     * code that may block (for example a database lookup).
     */
    public Uni<Map<String, Object>> createAccessTokenAsync(String refreshToken, UserTokenPayload payload) {
        if (StringUtils.isBlank(refreshToken)) return Uni.createFrom().failure(new BadRequestException("Missing refresh token!"));

        String sessionId;
        String tokenIssuer;
        try {
            JwtClaims claims = getRefreshTokenClaims(refreshToken);
            sessionId = claims.getSubject();
            // resolved here, on the request thread, because it may read the current request
            tokenIssuer = StringUtils.isBlank(claims.getIssuer()) ? getIssuer() : claims.getIssuer();
        } catch (Exception e) {
            return Uni.createFrom().failure(new RuntimeException(e.getMessage(), e));
        }

        if (payload != null) {
            return createAccessTokenAsync(payload, defaultExpiration(), tokenIssuer, sessionId, refreshToken);
        }

        return getUserIdAsync(sessionId)
                .emitOn(Infrastructure.getDefaultWorkerPool())
                .chain(userId -> {
                    try (InstanceHandle<SecurityLifeCycleService> handle = Arc.container().instance(SecurityLifeCycleService.class)) {
                        if (!handle.isAvailable()) throw new IllegalArgumentException("Please implement User Service");
                        UserTokenPayload createdPayload = handle.get().onCreateAccessTokenPayload(userId);
                        return createAccessTokenAsync(createdPayload, defaultExpiration(), tokenIssuer, sessionId, refreshToken);
                    } catch (IllegalArgumentException e) {
                        return Uni.<Map<String, Object>>createFrom().failure(new RuntimeException(e.getMessage(), e));
                    } catch (Exception e) {
                        return Uni.<Map<String, Object>>createFrom().failure(new BadRequestException(e.getMessage(), e));
                    }
                });
    }

    private Instant defaultExpiration() {
        return expiration == 0 ?
                Instant.now().plus(Duration.ofMinutes(30)) :
                Instant.now().plus(Duration.ofSeconds(expiration));
    }

    public Map<String, Object> createAccessToken(UserTokenPayload userTokenPayload, Instant expiredAt, String issuer, String sessionId, String existingRefreshToken) {
        IssuedTokens issued = issueTokens(userTokenPayload, expiredAt, issuer, sessionId, existingRefreshToken);
        KeyValueCacheUtils.saveCache(keySessionStorage, issued.sessionId(), userTokenPayload.getId());
        if (issued.cookieTokenKey() != null) {
            CookieSessionUtils.putSessionToCache(issued.cookieTokenKey(), issued.cookieToken());
        }
        return issued.response();
    }

    /**
     * Non-blocking variant of {@link #createAccessToken(UserTokenPayload)}: signing the tokens is done right away,
     * only the cache writes are asynchronous. Call it from the request thread, since resolving the issuer may need
     * the current request.
     */
    public Uni<Map<String, Object>> createAccessTokenAsync(UserTokenPayload userTokenPayload) {
        return createAccessTokenAsync(userTokenPayload, null, null, null, null);
    }

    private Uni<Map<String, Object>> createAccessTokenAsync(UserTokenPayload userTokenPayload, Instant expiredAt, String issuer, String sessionId, String existingRefreshToken) {
        IssuedTokens issued;
        try {
            issued = issueTokens(userTokenPayload, expiredAt, issuer, sessionId, existingRefreshToken);
        } catch (RuntimeException e) {
            return Uni.createFrom().failure(e);
        }

        Uni<Void> saveSession = KeyValueCacheUtils.saveCacheAsync(keySessionStorage, issued.sessionId(), userTokenPayload.getId());
        Uni<Void> saveCookie = issued.cookieTokenKey() == null ?
                Uni.createFrom().voidItem() :
                CookieSessionUtils.putSessionToCacheAsync(issued.cookieTokenKey(), issued.cookieToken());

        return Uni.combine().all().unis(saveSession, saveCookie).discardItems()
                .replaceWith(issued.response());
    }

    private record IssuedTokens(Map<String, Object> response, String sessionId, String cookieTokenKey, String cookieToken) {
    }

    private IssuedTokens issueTokens(UserTokenPayload userTokenPayload, Instant expiredAt, String issuer, String sessionId, String existingRefreshToken) {
        if (StringUtils.isBlank(tokenEncryptionSecret) || "-".equals(tokenEncryptionSecret)) throw new BadRequestException("Please set property \"" + PROP_SECURITY_TOKEN_ENCRYPTION_SECRET + "\".");
        if (StringUtils.isBlank(sessionId)) sessionId = UUID.randomUUID().toString();

        if (expiredAt == null) expiredAt = expiration == 0 ?
                Instant.now().plus(Duration.ofMinutes(30)) :
                Instant.now().plus(Duration.ofSeconds(expiration));

        if (StringUtils.isBlank(issuer)) issuer = getIssuer();
        String accessToken;
        String cookieTokenKey = null, cookieToken = null;
        if (Boolean.parseBoolean(securityIsEncryptAccessToken)) {
            accessToken = createClaimsForAccess(userTokenPayload, expiredAt, issuer).jws().keyId(sessionId).innerSign().encryptWithSecret(tokenEncryptionSecret);

            if (cookieTokenEnabled) {
                cookieTokenKey = UUID.randomUUID().toString();
                cookieToken = createClaimsForAccess(userTokenPayload, Instant.now().plus(Duration.ofDays(7)), issuer)
                        .jws().keyId(sessionId).innerSign().encryptWithSecret(tokenEncryptionSecret);
            }
        } else {
            accessToken = createClaimsForAccess(userTokenPayload, expiredAt, issuer).jws().keyId(sessionId).sign();

            if (cookieTokenEnabled) {
                cookieTokenKey = UUID.randomUUID().toString();
                cookieToken = createClaimsForAccess(userTokenPayload, Instant.now().plus(Duration.ofDays(7)), issuer)
                        .jws().keyId(sessionId).sign();
            }
        }

        String refreshToken = StringUtils.isBlank(existingRefreshToken) ? createClaimsForRefresh(sessionId, Instant.now().plus(Duration.ofDays(7)), issuer)
                .jws()
                .innerSign().encryptWithSecret(tokenEncryptionSecret) : existingRefreshToken;

        Map<String, Object> response =  new HashMap<>(Map.of(
                AuthenticationService.keyAccessToken, accessToken,
                AuthenticationService.keyRefreshToken, refreshToken,
                keyData, userTokenPayload
        ));

        if (cookieTokenEnabled) {
            response.put(keyCookieToken, cookieTokenKey);
        }
        response.put(AuthenticationService.keyExpiredAt, expiredAt.atZone(ZoneId.of("UTC")));
        return new IssuedTokens(response, sessionId, cookieTokenKey, cookieToken);
    }

    private JwtClaimsBuilder createClaimsForAccess(UserTokenPayload userTokenPayload, Instant expiredAt, String issuer) {
        JwtClaimsBuilder jwtClaimsBuilder = Jwt.claims()
                .preferredUserName(userTokenPayload.getUsername())
                .claim(Claims.email, userTokenPayload.getEmail())
                .claim(Claims.full_name, userTokenPayload.getFullName())
                .subject(userTokenPayload.getId())
                .issuer(issuer)
                .claim(permissions, userTokenPayload.getPermission())
                .expiresAt(expiredAt);

        if (userTokenPayload.getTenantAccess() != null && !userTokenPayload.getTenantAccess().isEmpty()) {
            jwtClaimsBuilder.claim(tenantAccess, userTokenPayload.getTenantAccess());
            if (StringUtils.isNotBlank(userTokenPayload.getCurrentTenant())) {
                jwtClaimsBuilder.claim(currentTenant, userTokenPayload.getCurrentTenant());
            }
        }

        if (userTokenPayload.getAttributes() != null && !userTokenPayload.getAttributes().isEmpty()) {
            for (String key : userTokenPayload.getAttributes().keySet()) {
                jwtClaimsBuilder.claim(key, userTokenPayload.getAttributes().get(key));
            }
        }

        return jwtClaimsBuilder;
    }

    private JwtClaimsBuilder createClaimsForRefresh(String sessionId, Instant expiredAt, String issuer) {
        return Jwt.claims().subject(sessionId)
                .issuer(issuer)
                .expiresAt(expiredAt);
    }

    public Response createAccessTokenResponse(UserTokenPayload userTokenPayload) {
        return Response.ok(createAccessToken(userTokenPayload)).build();
    }

    public Response createAccessTokenResponse(String refreshToken) {
        return Response.ok(createAccessToken(refreshToken)).build();
    }

    @Deprecated
    public Response createAccessTokenResponse(UserTokenPayload userTokenPayload, String sessionId) {
        return Response.ok(createAccessToken(userTokenPayload, null, null, null, null)).build();
    }

    @Deprecated
    public Response createAccessTokenResponse(UserTokenPayload userTokenPayload, String sessionId, String existingRefreshToken) {
        return Response.ok(createAccessToken(userTokenPayload, null, null, sessionId, existingRefreshToken)).build();
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
        String savedSessionById = KeyValueCacheUtils.findCache(keySessionStorage, sessionId);
        return userId.equals(savedSessionById);
    }

    public Uni<Boolean> checkSessionAsync(String sessionId, String userId) {
        if (StringUtils.isBlank(userId)) return Uni.createFrom().failure(new BadRequestException("Invalid user id"));
        return KeyValueCacheUtils.findCacheAsync(keySessionStorage, sessionId)
                .map(userId::equals);
    }

    public void removeSession(String sessionId) {
        KeyValueCacheUtils.removeCache(keySessionStorage, sessionId);
    }

    public Uni<Void> removeSessionAsync(String sessionId) {
        return KeyValueCacheUtils.removeCacheAsync(keySessionStorage, sessionId);
    }

    @Deprecated(forRemoval = true)
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

        if (StringUtils.isBlank(sessionId)) {
            throw new BadRequestException("Invalid refresh token");
        }
        return sessionId;
    }

    public JwtClaims getRefreshTokenClaims(String refreshToken) {
        if (StringUtils.isBlank(tokenEncryptionSecret)) throw new BadRequestException("Please set property \"" + PROP_SECURITY_TOKEN_ENCRYPTION_SECRET + "\".");
        if (StringUtils.isBlank(refreshToken)) throw new BadRequestException("Invalid refresh token");

        try {
            return getJwtConsumer().processToClaims(refreshToken);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage(), e);
        }
    }

    public String getUserId(String sessionId) {
        return KeyValueCacheUtils.findCache(keySessionStorage, sessionId);
    }

    public Uni<String> getUserIdAsync(String sessionId) {
        return KeyValueCacheUtils.findCacheAsync(keySessionStorage, sessionId);
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
        KeyValueCacheUtils.removeCache(keySessionStorage, sessionId);
    }

    public Uni<Void> logoutAsync(String sessionId) {
        return KeyValueCacheUtils.removeCacheAsync(keySessionStorage, sessionId);
    }
}
