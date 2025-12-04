package io.yanmastra.authentication.it;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class AuthenticationResourceTest {

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
//            payload.email = payload.id + "@onebyone.io";
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
}
