package io.yanmastra.authentication.utils;

import io.vertx.ext.web.RoutingContext;
import io.yanmastra.authentication.security.AuthenticationService;
import io.yanmastra.quarkusBase.utils.KeyValueCacheUtils;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CookieSessionUtils {
    public static final String AUTH_IDENTIFIER = "auth-identifier";
    private static final String COOKIE_SESSION = "cookie-session";
    private static final Logger log = Logger.getLogger(CookieSessionUtils.class);

    public static NewCookie createSessionCookie(Map<String, Object> authResponse) {
        String cookieToken = (String) authResponse.get(AuthenticationService.keyCookieToken);

        return new NewCookie.Builder(AUTH_IDENTIFIER)
                .path("/")
                .expiry(Date.from(Instant.now().plus(Duration.ofHours(3))))
                .value(cookieToken)
                .build();
    }

    public static Response.ResponseBuilder createSessionCookie(Map<String, Object> authResponse, Response.ResponseBuilder responseBuilder) {
        NewCookie cookie = createSessionCookie(authResponse);
        return responseBuilder.cookie(cookie);
    }

    public static void repeatCookie(RoutingContext context) {
        Map<String, String> cookies = getCookieFromHeader(context);

        if (cookies != null && cookies.containsKey(AUTH_IDENTIFIER)) {
            String setCookie = AUTH_IDENTIFIER + '=' +
                    cookies.get(AUTH_IDENTIFIER) + ";Path=/;Max-Age=" + 10800+';';

            context.response().putHeader(HttpHeaders.SET_COOKIE, setCookie.trim());
        }
    }

    public static Map<String, String> getCookieFromHeader(RoutingContext context) {
        String rawCookie = context.request().getHeader(HttpHeaders.COOKIE);
        if (StringUtils.isBlank(rawCookie)) return null;

        Map<String, String> authResponse = new HashMap<>();
        for (String cookie : rawCookie.split(";")) {
            String[] cookieParts = cookie.trim().replace(" ", "").split("=", 2);
            if (cookieParts.length == 2) {
                authResponse.put(cookieParts[0], cookieParts[1]);
            }
        }
        return authResponse;
    }

    public static String getSessionValue(String key) {
        return KeyValueCacheUtils.findCache(COOKIE_SESSION, key);
    }

    public static void putSessionToCache(String key, String value) {
        KeyValueCacheUtils.saveCache(COOKIE_SESSION, key, value);
    }

}
