package io.yanmastra.quarkus.rediscache;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.yanmastra.quarkusBase.utils.KeyValueCacheUtils;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import org.junit.jupiter.api.Test;

import java.net.HttpCookie;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * The real client of the cache: cookie sessions of quarkus-authentication. Authentication runs on the event loop, so a
 * cookie request that succeeds here proves the cache is read without blocking it.
 */
@QuarkusTest
class CookieSessionOverRedisTest {

    @Inject
    RedisDataSource redis;
    @Inject
    RedisCacheConfig config;

    private static String cookieOf(Response response) {
        HttpCookie cookie = HttpCookie.parse(response.header(HttpHeaders.SET_COOKIE)).get(0);
        return cookie.getName() + "=" + cookie.getValue();
    }

    private static String cookieKeyOf(Response response) {
        return HttpCookie.parse(response.header(HttpHeaders.SET_COOKIE)).get(0).getValue();
    }

    @Test
    void cookieSessionCreatedByABlockingEndpointAuthenticatesThroughRedis() {
        Response login = given().accept(ContentType.JSON).when().get("/session/login").then().statusCode(200).extract().response();

        given().header(HttpHeaders.COOKIE, cookieOf(login))
                .when().get("/session/me")
                .then().statusCode(200);

        assertFalse(redis.hash(String.class).hgetall(config.keyPrefix() + ":cookie-session").isEmpty(),
                "the cookie session must be stored in Redis");
    }

    @Test
    void cookieSessionCreatedByAReactiveEndpointAuthenticatesThroughRedis() {
        Response login = given().accept(ContentType.JSON).when().get("/session/login-async").then().statusCode(200).extract().response();

        given().header(HttpHeaders.COOKIE, cookieOf(login))
                .when().get("/session/me")
                .then().statusCode(200);
    }

    @Test
    void bearerTokenStillWorks() {
        Response login = given().accept(ContentType.JSON).when().get("/session/login").then().statusCode(200).extract().response();

        given().header(HttpHeaders.AUTHORIZATION, "Bearer " + login.jsonPath().getString("access_token"))
                .when().get("/session/me")
                .then().statusCode(200);
    }

    @Test
    void cookieIsRejectedOnceItsSessionIsGoneFromRedis() {
        Response login = given().accept(ContentType.JSON).when().get("/session/login").then().statusCode(200).extract().response();
        String cookie = cookieOf(login);

        given().header(HttpHeaders.COOKIE, cookie).when().get("/session/me").then().statusCode(200);

        KeyValueCacheUtils.removeCache("cookie-session", cookieKeyOf(login));

        given().header(HttpHeaders.COOKIE, cookie).when().get("/session/me").then().statusCode(401);
    }
}
