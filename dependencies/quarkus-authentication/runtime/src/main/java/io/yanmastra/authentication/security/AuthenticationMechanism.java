package io.yanmastra.authentication.security;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.security.identity.request.TokenAuthenticationRequest;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import io.yanmastra.authentication.service.SecurityLifeCycleService;
import io.yanmastra.authentication.utils.CookieSessionUtils;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.yanmastra.authentication.utils.CookieSessionUtils.TIMEZONE;

public class AuthenticationMechanism implements HttpAuthenticationMechanism {

    @Inject
    JWTParser jwtParser;
    @Inject
    Logger log;

    @Inject
    Instance<SecurityLifeCycleService> securityLifeCycleService;

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext authContext, IdentityProviderManager identityProviderManager) {
        if (!(authContext.get(HttpAuthenticationMechanism.class.getName()) instanceof AuthenticationMechanism)) {
            authContext.put(HttpAuthenticationMechanism.class.getName(), this);
        }

        if (isSkipAuthorisation(authContext)) {
            return Uni.createFrom().nullItem();
        }

        String token = getTokenFromCookie(authContext);

        if (StringUtils.isBlank(token)) {
            token = getTokenFromHeader(authContext);
        }

        if (StringUtils.isNotBlank(token)) {
            try {
                UserPrincipal principal = (UserPrincipal) jwtParser.parse(token);
                principal.setTimezone(getTimezoneFromCookie(authContext));

                CookieSessionUtils.repeatCookie(authContext);
                TokenAuthenticationRequest request = new TokenAuthenticationRequest(principal.getCredential());
                return identityProviderManager.authenticate(request)
                        .onFailure()
                        .invoke(throwable -> log.error(throwable.getMessage(), throwable));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        return Uni.createFrom().optional(Optional.empty());
    }

    private String getTimezoneFromCookie(RoutingContext context) {
        Map<String, String> cookies = CookieSessionUtils.getCookieFromHeader(context);
        if (cookies != null && cookies.containsKey(TIMEZONE)) {
            return URLDecoder.decode(cookies.get(TIMEZONE), StandardCharsets.UTF_8);
        }
        return ZoneId.systemDefault().getId();
    }

    private String getTokenFromHeader(RoutingContext context) {
        if (context.request().headers().contains(HttpHeaders.AUTHORIZATION)) {
            String token = context.request().getHeader(HttpHeaders.AUTHORIZATION);
            if (StringUtils.isNotBlank(token)) {
                return token.replace("Bearer ", "");
            }
        }
        return null;
    }

    private boolean isSkipAuthorisation(RoutingContext context) {
        Optional<SecurityLifeCycleService> opsSecLifeCycleService = securityLifeCycleService.stream().findFirst();
        return opsSecLifeCycleService.isEmpty() || opsSecLifeCycleService.get().isSkipAuthorisation(context.request().path());
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        ChallengeData challengeData = null;

        Optional<SecurityLifeCycleService> opsSecLifeCycleService = securityLifeCycleService.stream().findFirst();
        if (opsSecLifeCycleService.isPresent()) {
            challengeData = opsSecLifeCycleService.get().onUnauthorizedError(context.request().path(), context.request().headers());
        } else {
            challengeData = new ChallengeData(HttpResponseStatus.UNAUTHORIZED.code(), null, null);
        }
        return Uni.createFrom().item(challengeData);
    }

    @Override
    public Set<Class<? extends AuthenticationRequest>> getCredentialTypes() {
        return Set.of(TokenAuthenticationRequest.class);
    }

    private String getTokenFromCookie(RoutingContext context) {
        Map<String, String> cookies = CookieSessionUtils.getCookieFromHeader(context);

        if (cookies != null && cookies.containsKey(CookieSessionUtils.AUTH_IDENTIFIER)) {
            String identifier1 = cookies.get(CookieSessionUtils.AUTH_IDENTIFIER);
            return CookieSessionUtils.getSessionValue(identifier1);
        }
        return null;
    }
}
