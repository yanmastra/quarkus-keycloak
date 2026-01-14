package io.yanmastra.quarkus.microservices.common.it;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.json.Json;
import jakarta.ws.rs.core.MediaType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

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

    @Test
    public void testSampleEntityInteger() {
        String category = UUID.randomUUID().toString();

        given()
                .when().get("/api/v1/sample-entity?category=" + category)
                .then()
                .statusCode(200)
                .body("meta.total_data", Matchers.equalTo(0));


        given()
                .body(Json.createObjectBuilder()
                        .add("name", UUID.randomUUID().toString())
                        .add("category", category)
                        .add("price", 5000)
                        .add("is_active", false)
                        .add("x_date", "2026-01-01")
                        .build()
                        .toString()
                )
                .contentType(MediaType.APPLICATION_JSON.toString())
                .post("/api/v1/sample-entity")
                .then()
                .statusCode(200);

        given()
                .body(Json.createObjectBuilder()
                        .add("name", UUID.randomUUID().toString())
                        .add("category", category)
                        .add("price", 4000)
                        .add("is_active", false)
                        .add("x_date", "2026-01-02")
                        .build()
                        .toString()
                )
                .contentType(MediaType.APPLICATION_JSON.toString())
                .post("/api/v1/sample-entity")
                .then()
                .statusCode(200);

        given()
                .body(Json.createObjectBuilder()
                        .add("name", UUID.randomUUID().toString())
                        .add("category", category)
                        .add("price", 2000)
                        .add("is_active", false)
                        .add("x_date", "2026-01-03")
                        .build()
                        .toString()
                )
                .contentType(MediaType.APPLICATION_JSON.toString())
                .post("/api/v1/sample-entity")
                .then()
                .statusCode(200);

        given()
                .when().get("api/v1/sample-entity/generate?count=10")
                .then()
                .statusCode(200);

        given()
                .when().get("/api/v1/sample-entity?category="+category)
                .then()
                .statusCode(200)
                .body("meta.total_data", Matchers.greaterThan(0));


        given()
                .when().get("/api/v1/sample-entity?price=greaterThan,3999")
                .then()
                .statusCode(200)
                .body("meta.total_data", Matchers.greaterThan(0));

        given()
                .when().get("/api/v1/sample-entity?price=in,2000,4000")
                .then()
                .statusCode(200)
                .body("meta.total_data", Matchers.greaterThan(0));

        given()
                .when().get("/api/v1/sample-entity?date=range,2026-01-01,2026-01-03")
                .then()
                .statusCode(200)
                .body("meta.total_data", Matchers.greaterThan(0));
    }
}
