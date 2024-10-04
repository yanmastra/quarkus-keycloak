package io.onebyone.quarkus.microservices.common.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class QuarkusMicroservicesCommonResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/quarkus-microservices-common")
                .then()
                .statusCode(200)
                .body(is("Hello quarkus-microservices-common"));
    }
}
