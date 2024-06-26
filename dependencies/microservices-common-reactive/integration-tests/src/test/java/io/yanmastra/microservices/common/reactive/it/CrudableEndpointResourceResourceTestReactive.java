package io.yanmastra.microservices.common.reactive.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class CrudableEndpointResourceResourceTestReactive {

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/crud-endpoint")
                .then()
                .statusCode(200)
                .body(is("Hello crud-endpoint"));
    }
}
