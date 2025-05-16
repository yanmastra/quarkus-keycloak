package io.yanmastra.authentication.security;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AnonymousAuthenticationRequest;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.security.identity.request.TokenAuthenticationRequest;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import io.yanmastra.authentication.utils.CookieSessionUtils;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.Set;

public class AuthenticationMechanism implements HttpAuthenticationMechanism {

    @Inject
    JWTParser jwtParser;
    @Inject
    Logger log;

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext authContext, IdentityProviderManager identityProviderManager) {
        String token = getTokenFromCookie(authContext);

        if (StringUtils.isBlank(token)) {
            token = getTokenFromHeader(authContext);
        }

        if (StringUtils.isNotBlank(token)) {
            try {
                UserPrincipal principal = (UserPrincipal) jwtParser.parse(token);
                CookieSessionUtils.repeatCookie(authContext);
                TokenAuthenticationRequest request = new TokenAuthenticationRequest(principal.getCredential());
                return identityProviderManager.authenticate(request);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        return identityProviderManager.authenticate(new AnonymousAuthenticationRequest());
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

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        return Uni.createFrom().item(new ChallengeData(HttpResponseStatus.UNAUTHORIZED.code(), null, null));
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
