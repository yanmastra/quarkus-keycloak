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
import jakarta.ws.rs.BadRequestException;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

import java.security.PublicKey;

import static io.onebyone.authentication.security.Constant.*;

@ApplicationScoped
@Alternative
@Priority(1)
public class UserJWTCallerPrincipalFactory extends JWTCallerPrincipalFactory {
    @ConfigProperty(name = PROP_MP_PUBLIC_KEY_LOCATION, defaultValue = "-")
    String publicKeyLocation;
    @ConfigProperty(name = PROP_ALLOWED_JWT_ISSUER, defaultValue = "*")
    String allowedJwtIssuer;
    @ConfigProperty(name = PROP_SECURITY_IS_ENCRYPT_ACCESS_TOKEN, defaultValue = "false")
    String securityIsEncryptAccessToken;
    @ConfigProperty(name = PROP_SECURITY_TOKEN_ENCRYPTION_SECRET, defaultValue = "-")
    String encryptSecretKey;

    private JwtConsumer jwtConsumer = null;
    private JwtConsumer getJwtConsumer() {
        if (jwtConsumer == null) {
            if (StringUtils.isBlank(publicKeyLocation) || "-".equals(publicKeyLocation)) throw new RuntimeException("Missing required property " + PROP_MP_PUBLIC_KEY_LOCATION);
            try {
                PublicKey publicKey = KeyUtils.readPublicKey(publicKeyLocation);
                JwtConsumerBuilder jcb = new JwtConsumerBuilder()
                        .setRequireExpirationTime()
                        .setVerificationKey(publicKey)
                        .setRelaxVerificationKeyValidation();

                jcb = AuthenticationService.getJwtConsumerBuilder(jcb, allowedJwtIssuer);

                if (Boolean.parseBoolean(securityIsEncryptAccessToken)) {
                    if (StringUtils.isBlank(encryptSecretKey) || "-".equals(encryptSecretKey)) {
                        throw new RuntimeException("Missing required property " + PROP_SECURITY_TOKEN_ENCRYPTION_SECRET);
                    }

                    jcb = jcb.setDecryptionKey(KeyUtils.createSecretKeyFromSecret(encryptSecretKey));
                }
                jwtConsumer = jcb.build();
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        return jwtConsumer;
    }

    @Override
    public JWTCallerPrincipal parse(String token, JWTAuthContextInfo jwtAuthContextInfo) throws HttpException {
        JwtClaims claims;
        try {
            JwtConsumer jwtConsumer = getJwtConsumer();
            claims = jwtConsumer.processToClaims(token);
        } catch (InvalidJwtException ex) {
            throw new HttpException(HttpResponseStatus.UNAUTHORIZED.code(), ex);
        } catch (RuntimeException ex) {
            throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), ex);
        }

        try {
            return new UserPrincipal(claims, new JsonWebTokenCredential(token));
        } catch (Exception e) {
            if (e instanceof HttpException httpException) {
                throw httpException;
            }

            throw new BadRequestException(e.getMessage());
        }
    }
}
