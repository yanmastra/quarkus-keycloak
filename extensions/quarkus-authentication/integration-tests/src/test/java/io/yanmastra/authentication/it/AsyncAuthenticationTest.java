package io.yanmastra.authentication.it;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.ws.rs.core.HttpHeaders;
import org.junit.jupiter.api.Test;

import java.net.HttpCookie;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

/** The same session flows as the blocking API, driven through reactive (Uni) endpoints on the event loop. */
@QuarkusTest
class AsyncAuthenticationTest {

    private Response login() {
        return given().accept(ContentType.JSON)
                .when().get("/authentication/async")
                .then().statusCode(200).extract().response();
    }

    private String cookieOf(Response response) {
        HttpCookie cookie = HttpCookie.parse(response.header(HttpHeaders.SET_COOKIE)).get(0);
        return cookie.getName() + "=" + cookie.getValue();
    }

    @Test
    void tokenCreatedAsyncAuthenticatesWithTheBearerToken() {
        String accessToken = login().jsonPath().getString("access_token");

        given().header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .when().get("/authentication/user")
                .then().statusCode(200);
    }

    @Test
    void cookieCreatedAsyncAuthenticatesTheNextRequest() {
        String cookie = cookieOf(login());

        given().header(HttpHeaders.COOKIE, cookie)
                .when().get("/authentication/user")
                .then().statusCode(200);
    }

    @Test
    void refreshTokenGivesANewWorkingAccessTokenAsync() {
        Response first = login();
        String refreshToken = first.jsonPath().getString("refresh_token");

        Response refreshed = given().contentType(ContentType.JSON).body(Map.of("refresh_token", refreshToken))
                .when().post("/authentication/async/refresh")
                .then().statusCode(200).extract().response();

        given().header(HttpHeaders.AUTHORIZATION, "Bearer " + refreshed.jsonPath().getString("access_token"))
                .when().get("/authentication/user")
                .then().statusCode(200);
    }

    @Test
    void sessionIsStoredCheckedAndRemovedAsync() {
        String refreshToken = login().jsonPath().getString("refresh_token");
        Map<String, String> body = Map.of("refresh_token", refreshToken);

        String userId = given().contentType(ContentType.JSON).body(body)
                .when().post("/authentication/async/session")
                .then().statusCode(200).body(not(emptyString())).extract().asString();

        given().contentType(ContentType.JSON).body(body).queryParam("userId", userId)
                .when().post("/authentication/async/session/check")
                .then().statusCode(200).body(equalTo("true"));
        given().contentType(ContentType.JSON).body(body).queryParam("userId", "someone-else")
                .when().post("/authentication/async/session/check")
                .then().statusCode(200).body(equalTo("false"));

        given().contentType(ContentType.JSON).body(body)
                .when().post("/authentication/async/logout")
                .then().statusCode(204);

        given().contentType(ContentType.JSON).body(body)
                .when().post("/authentication/async/session")
                .then().statusCode(204);
    }
}
