package io.onebyone.authentication.security;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.smallrye.jwt.runtime.auth.JsonWebTokenCredential;
import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import io.smallrye.jwt.auth.principal.JWTCallerPrincipal;
import io.smallrye.jwt.auth.principal.JWTCallerPrincipalFactory;
import io.smallrye.jwt.util.KeyUtils;
import io.vertx.ext.web.handler.HttpException;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

import java.security.GeneralSecurityException;
import java.util.Set;
import java.util.stream.Collectors;

import static io.onebyone.authentication.security.Constant.PROP_ALLOWED_JWT_ISSUER;
import static io.onebyone.authentication.security.Constant.PROP_ALLOWED_JWT_ORIGIN;

@ApplicationScoped
@Alternative
@Priority(1)
public class UserJWTCallerPrincipalFactory extends JWTCallerPrincipalFactory {
    @ConfigProperty(name = PROP_ALLOWED_JWT_ISSUER, defaultValue = "*")
    String allowedJwtIssuer;
    @ConfigProperty(name = PROP_ALLOWED_JWT_ORIGIN, defaultValue = "*")
    String allowedJwtOrigin;
    @Inject
    Logger log;

    private Set<String> allowedJwtIssuers = null;
    private Set<String> allowedJwtOrigins = null;

    @Override
    public JWTCallerPrincipal parse(String token, JWTAuthContextInfo jwtAuthContextInfo) throws HttpException {
        JwtClaims claims;
        try {
            JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                    .setRequireExpirationTime()
                    .setVerificationKey(KeyUtils.decodePublicKey(jwtAuthContextInfo.getPublicKeyContent()))
                    .setRelaxVerificationKeyValidation()
                    .build();

            claims = jwtConsumer.processToClaims(token);
        } catch (InvalidJwtException ex) {
            throw new HttpException(HttpResponseStatus.UNAUTHORIZED.code(), ex);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }

        try {
            if (!isAllowedJwtIssuer(claims.getIssuer()))
                throw new HttpException(HttpResponseStatus.UNAUTHORIZED.code(), "{\"message\": \"Invalid Issuer\"}");

            return new UserPrincipal(claims, new JsonWebTokenCredential(token));
        } catch (Exception e) {
            if (e instanceof HttpException httpException) {
                throw httpException;
            }

            throw new BadRequestException(e.getMessage());
        }
    }

    private boolean isAllowedJwtIssuer(String jwtIssuer) {
        if ("*".equals(allowedJwtIssuer)) return true;
        if (allowedJwtIssuers == null) {
            if (StringUtils.isNotBlank(allowedJwtIssuer)) {
                allowedJwtIssuers = fetchUrl(allowedJwtIssuer);
            } else
                log.warn("Please set '" + PROP_ALLOWED_JWT_ISSUER + "' property");
        }

        return allowedJwtIssuers.contains(jwtIssuer);
    }

    private Set<String> fetchUrl(String urlList) {
        return Set.of(urlList.split(",")).stream()
                .map(iss -> {
                    if (!iss.startsWith("http://") && !iss.startsWith("https://")) {
                        return "https://"+iss;
                    }
                    if (iss.endsWith("/")) return iss.substring(0,iss.length()-1);
                    return iss;
                }).collect(Collectors.toSet());
    }

    private boolean isAllowedJwtOrigin(Set<String> allowedJwtOrigins) {
        return allowedJwtOrigins != null && allowedJwtOrigins.stream().anyMatch(this::isAllowedJwtOrigin);
    }

    private boolean isAllowedJwtOrigin(String jwtOrigin) {
        if ("*".equals(allowedJwtOrigin)) return true;
        if (allowedJwtOrigins == null) {
            if (StringUtils.isNotBlank(allowedJwtOrigin)) {
                allowedJwtOrigins = fetchUrl(jwtOrigin);
            } else
                log.warn("Please set '" + PROP_ALLOWED_JWT_ORIGIN + "' property");
        }
        return allowedJwtOrigins.contains(jwtOrigin);
    }
}
