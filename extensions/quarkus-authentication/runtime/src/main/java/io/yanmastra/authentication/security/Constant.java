package io.yanmastra.authentication.security;

import io.smallrye.jwt.algorithm.SignatureAlgorithm;
import io.smallrye.jwt.util.KeyUtils;
import io.smallrye.jwt.util.ResourceUtils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.PublicKey;

public interface Constant {
    String PROP_ALLOWED_JWT_ISSUER = "security.jwt.allowed.issuer";
    String PROP_ALLOWED_JWT_ORIGIN = "security.jwt.allowed.origin";
    String PROP_SECURITY_TOKEN_ENCRYPTION_SECRET = "security.jwt.token.encryption.secret";
    String PROP_SECURITY_IS_ENCRYPT_ACCESS_TOKEN = "security.jwt.isencrypt.accessToken";
    String PROP_MP_PUBLIC_KEY_LOCATION = "mp.jwt.verify.publickey.location";
    String PROP_SMALLRYE_JWT_ISSUER = "smallrye.jwt.new-token.issuer";
    String PROP_MP_JWT_VERIFY_CLOCK_SKEW = "mp.jwt.verify.clock.skew";
    String PROP_COOKIE_TOKEN_ENABLED = "cookie.token.enabled";


    static PublicKey getPublicKey(String publicKeyLocation) throws Exception {
        InputStream is = ResourceUtils.getAsClasspathResource(publicKeyLocation);
        if (is == null) {
            throw new FileNotFoundException("File not found in classpath: " + publicKeyLocation);
        }
        try (final InputStream finalIs = is) {
            byte[] tmp = new byte[4096];
            int length = finalIs.read(tmp);
            return KeyUtils.decodePublicKey(new String(tmp, 0, length), SignatureAlgorithm.RS256);
        }
    }
}
