package io.yanmastra.authentication.it;

import com.fasterxml.jackson.core.type.TypeReference;
import io.quarkus.test.junit.QuarkusTest;
import io.yanmastra.authentication.security.AuthenticationService;
import io.yanmastra.quarkusBase.utils.JsonUtils;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import org.apache.http.HttpStatus;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.MediaType;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthenticationResourceTest {

    @Inject
    Logger log;

    @Test
    public void testError() {
        given().basePath("/authentication/error_500")
                .get()
                .then()
                .statusCode(500);
    }

    @Test
    public void testPost() {
        for (int i = 0; i < 100; i++) {
//            TestPayload payload = new TestPayload();
//            payload.id = UUID.randomUUID().toString();
//            payload.email = payload.id + "@yanmastra.io";
//            payload.name = payload.id.toUpperCase().replace("-", " ");
//            payload.phone = "08"+i+"10927307103";
//
//            given().basePath("/authentication/test-post")
//                    .contentType(ContentType.JSON)
//                    .body(JsonUtils.toJson(payload))
//                    .post()
//                    .then()
//                    .statusCode(200)
//                    .contentType(ContentType.JSON)
//                    .body("id", Matchers.notNullValue())
//                    .body("email", Matchers.notNullValue())
//                    .body("phone", Matchers.notNullValue())
//                    .body("name", Matchers.notNullValue());
        }
    }

    private String accessToken;
    private String refreshToken;
    private String cookieValue;

    @BeforeEach
    public void getToken() {
        io.restassured.response.Response response = given().accept(MediaType.APPLICATION_JSON.toString())
                .when().get("/authentication")
                .then().statusCode(200).extract().response();

        String cookieToken = response.header(HttpHeaders.SET_COOKIE);
        List<HttpCookie> cookies = HttpCookie.parse(cookieToken);
        List<String> cookiesList = new ArrayList<>();
        for (HttpCookie cookie: cookies) {
            cookiesList.add(cookie.getName() + "=" + cookie.getValue());
        }
        cookieValue = String.join(";", cookiesList);

        String json = response.getBody().asString();
//        log.info("response: " + json);
        Map<String, Object> responseObject = JsonUtils.fromJson(json, new TypeReference<>() {
        });
        accessToken = (String) responseObject.get(AuthenticationService.keyAccessToken);
        refreshToken = (String) responseObject.get(AuthenticationService.keyRefreshToken);
    }

    @Test
    void testGetUser() {
        Map<String, String> headers = new HashMap<>(Map.of(
                HttpHeaders.AUTHORIZATION, "Bearer " + accessToken
        ));
        given().accept(MediaType.APPLICATION_JSON.toString())
                .headers(headers)
                .when().get("/authentication/user")
                .then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    void testGetUserByCookie() {
        given().accept(MediaType.APPLICATION_JSON.toString())
                .headers(Map.of(HttpHeaders.COOKIE, cookieValue))
                .when().get("/authentication/user")
                .then().statusCode(HttpStatus.SC_OK);

        given().accept(MediaType.APPLICATION_JSON.toString())
                .headers(Map.of(HttpHeaders.COOKIE, cookieValue))
                .when().get("/authentication/user_get_data")
                .then().statusCode(HttpStatus.SC_OK);


        given().accept(MediaType.APPLICATION_JSON.toString())
                .headers(Map.of(HttpHeaders.COOKIE, cookieValue))
                .when().get("/authentication/forbidden")
                .then().statusCode(HttpStatus.SC_FORBIDDEN);
    }


}
