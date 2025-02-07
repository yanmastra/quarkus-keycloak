package io.onebyone.authentication.it;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusIntegrationTest
public class AuthenticationResourceIT extends AuthenticationResourceTest {

    @Test
    public void testError() {
        given().basePath("/authentication/error_500")
                .get()
                .then()
                .statusCode(500);
    }
}
