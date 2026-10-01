package io.yanmastra.authentication.it;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.smallrye.mutiny.Uni;
import io.yanmastra.quarkusBase.cache.KeyValueCacheStore;
import io.yanmastra.quarkusBase.utils.KeyValueCacheUtils;
import jakarta.ws.rs.core.HttpHeaders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.HttpCookie;
import java.util.Map;
import java.util.Set;

import static io.restassured.RestAssured.given;

/**
 * Failure policy: when the cache cannot be reached (e.g. Redis is down), the session cookie is treated as absent.
 * The request then falls back to the Authorization header and is rejected only if that is missing too.
 */
@QuarkusTest
class CacheUnavailableTest {

    /**
     * Behaves like a remote store whose server goes down after start-up: reads and writes fail. Migration writes are
     * accepted so that the store can be registered.
     */
    static class UnreachableStore implements KeyValueCacheStore {
        private static RuntimeException down() {
            return new IllegalStateException("cache server unreachable");
        }

        @Override public String get(String cacheName, String key) { throw down(); }
        @Override public void put(String cacheName, String key, String value) { throw down(); }
        @Override public void remove(String cacheName, String key) { throw down(); }
        @Override public Uni<String> getAsync(String cacheName, String key) { return Uni.createFrom().failure(down()); }
        @Override public Set<String> listCacheNames() { return Set.of(); }
        @Override public Map<String, String> entries(String cacheName) { return Map.of(); }
        @Override public boolean putIfAbsent(String cacheName, String key, String value) { return true; }
        @Override public void markMigrated(String cacheName) { }
    }

    private final UnreachableStore unreachable = new UnreachableStore();
    private String accessToken;
    private String cookie;

    @BeforeEach
    void login() {
        Response response = given().accept(ContentType.JSON)
                .when().get("/authentication")
                .then().statusCode(200).extract().response();
        accessToken = response.jsonPath().getString("access_token");
        HttpCookie httpCookie = HttpCookie.parse(response.header(HttpHeaders.SET_COOKIE)).get(0);
        cookie = httpCookie.getName() + "=" + httpCookie.getValue();
    }

    @AfterEach
    void restoreFileStore() {
        KeyValueCacheUtils.unregisterStore(unreachable);
    }

    @Test
    void fallsBackToTheAuthorizationHeaderWhenTheCacheIsDown() {
        KeyValueCacheUtils.registerStore(unreachable);

        given().header(HttpHeaders.COOKIE, cookie)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .when().get("/authentication/user")
                .then().statusCode(200);
    }

    @Test
    void rejectsACookieOnlyRequestWhenTheCacheIsDown() {
        KeyValueCacheUtils.registerStore(unreachable);

        given().header(HttpHeaders.COOKIE, cookie)
                .when().get("/authentication/user")
                .then().statusCode(401);
    }
}
