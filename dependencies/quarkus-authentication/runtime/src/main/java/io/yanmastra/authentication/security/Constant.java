package io.yanmastra.authentication.security;

public interface Constant {
    String PROP_ALLOWED_JWT_ISSUER = "security.jwt.allowed.issuer";
    String PROP_ALLOWED_JWT_ORIGIN = "security.jwt.allowed.origin";
    String PROP_SECURITY_TOKEN_ENCRYPTION_SECRET = "security.jwt.token.encryption.secret";
    String PROP_SECURITY_IS_ENCRYPT_ACCESS_TOKEN = "security.jwt.isencrypt.accessToken";
    String PROP_MP_PUBLIC_KEY_LOCATION = "mp.jwt.verify.publickey.location";
    String PROP_SMALLRYE_JWT_ISSUER = "smallrye.jwt.new-token.issuer";
    String PROP_MP_JWT_VERIFY_CLOCK_SKEW = "mp.jwt.verify.clock.skew";
    String PROP_COOKIE_TOKEN_ENABLED = "cookie.token.enabled";
}
