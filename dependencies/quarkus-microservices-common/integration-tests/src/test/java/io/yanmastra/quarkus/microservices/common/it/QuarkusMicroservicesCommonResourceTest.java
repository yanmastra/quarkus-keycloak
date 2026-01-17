package io.yanmastra.quarkus.microservices.common.it;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.yanmastra.quarkus.microservices.common.it.json.SampleChildEntityJson;
import io.yanmastra.quarkus.microservices.common.it.json.SampleEntityJson;
import io.yanmastra.quarkusBase.ResponseJson;
import io.yanmastra.quarkusBase.utils.DateTimeUtils;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import org.hamcrest.Matchers;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.MediaType;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class QuarkusMicroservicesCommonResourceTest {

    @Inject
    Logger logger;

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
                .when().get("/api/v1/sample-entity?price=in,2000,4000")
                .then()
                .statusCode(200)
                .body("meta.total_data", Matchers.greaterThan(0));

        given()
                .when().get("/api/v1/sample-entity?price=greaterThan,3999")
                .then()
                .statusCode(200)
                .body("meta.total_data", Matchers.greaterThan(0));


        given()
                .when().get("/api/v1/sample-entity?date=range,2026-01-01,2026-01-03")
                .then()
                .statusCode(200)
                .body("meta.total_data", Matchers.greaterThan(0));
    }

    @Test
    public void testFilterDateTimeField() {
        String category = UUID.randomUUID().toString();
        ZonedDateTime dateTime = ZonedDateTime.now().withZoneSameLocal(ZoneId.of("Asia/Makassar"));
        given()
                .body(Json.createObjectBuilder()
                        .add("name", UUID.randomUUID().toString())
                        .add("category", category)
                        .add("price", 2000)
                        .add("is_active", false)
                        .add("x_date", "2026-01-03")
                        .add("x_date_time", DateTimeUtils.utcDateDtf.format(dateTime))
                        .build()
                        .toString()
                )
                .contentType(MediaType.APPLICATION_JSON.toString())
                .post("/api/v1/sample-entity")
                .then()
                .statusCode(200);

        given()
                .when().get("/api/v1/sample-entity?dateTime="+DateTimeUtils.toDateOnly(dateTime)+"&category="+category)
                .then()
                .statusCode(200)
                .body("meta.total_data", Matchers.equalTo(1));

        ZonedDateTime filterStart = dateTime.minusHours(1);
        ZonedDateTime filterEnd = dateTime.plusHours(1);

        given()
                .when().get("/api/v1/sample-entity?dateTime=range,"+
                        DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(filterStart) + "," +
                        DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(filterEnd)+
                        "&category="+category)
                .then()
                .statusCode(200)
                .body("meta.total_data", Matchers.equalTo(1));
    }


    @Test
    public void testFilterDateTimeFieldAndSort() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        String[] categories = new String[]{"C_A_" + unique, "C_B_" + unique, "C_C_" + unique};
        ZonedDateTime dateTime = ZonedDateTime.now().withZoneSameLocal(ZoneId.of("Asia/Makassar"));

        Map<String, Float> ctgMax = new HashMap<>();
        Map<String, Integer> ctgCount = new HashMap<>();
        for (int i = 1; i <= 20; i++) {
            String category = categories[(i-1)%3];
            float price = i * 10000F;
            given()
                    .body(Json.createObjectBuilder()
                            .add("name", UUID.randomUUID().toString())
                            .add("category", category)
                            .add("price", price)
                            .add("is_active", (i%3) == 1)
                            .add("x_date", DateTimeUtils.toDateOnly(LocalDate.now()))
                            .add("x_date_time", DateTimeUtils.utcDateDtf.format(dateTime))
                            .build()
                            .toString()
                    )
                    .contentType(MediaType.APPLICATION_JSON.toString())
                    .post("/api/v1/sample-entity")
                    .then()
                    .statusCode(200);
            Float max = ctgMax.computeIfAbsent(category, c -> 0F);
            if (price > max) {
                ctgMax.put(category, price);
            }

            int count = ctgCount.computeIfAbsent(category, c -> 0);
            ctgCount.put(category, count +1);
        }

        ZonedDateTime filterStart = dateTime.minusHours(1);
        ZonedDateTime filterEnd = dateTime.plusHours(1);

        given()
                .when().get("/api/v1/sample-entity?dateTime=range,"+
                        DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(filterStart) + "," +
                        DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(filterEnd)+
                        "&category=in,"+ String.join(",", categories) +
                        "&sort=price,ASC" +
                        "&page=1&size=20"
                )
                .then()
                .statusCode(200)
                .body("meta.total_data", Matchers.equalTo(20))
                .body("data[0].price", Matchers.equalTo(10000.0F))
                .body("data[19].price", Matchers.equalTo(200000.0F));

        given()
                .when().get("/api/v1/sample-entity?dateTime=range,"+
                        DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(filterStart) + "," +
                        DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(filterEnd)+
                        "&category=in,"+ String.join(",", categories) +
                        "&sort=price,DESC" +
                        "&page=1&size=20"
                )
                .then()
                .statusCode(200)
                .body("meta.total_data", Matchers.equalTo(20))
                .body("data[19].price", Matchers.equalTo(10000.0F))
                .body("data[0].price", Matchers.equalTo(200000.0F));

        int secondIndex = ctgCount.get(categories[0]);
        int thirdIndex = secondIndex + ctgCount.get(categories[1]);

        given()
                .when().get("/api/v1/sample-entity?dateTime=range,"+
                        DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(filterStart) + "," +
                        DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(filterEnd)+
                        "&category=in,"+ String.join(",", categories) +
                        "&sort=category,ASC,price,DESC" +
                        "&page=1&size=20"
                )
                .then()
                .statusCode(200)
                .body("meta.total_data", Matchers.equalTo(20))
                .body("data[0].price", Matchers.equalTo(ctgMax.get(categories[0])))
                .body("data["+secondIndex+"].price", Matchers.equalTo(ctgMax.get(categories[1])))
                .body("data["+thirdIndex+"].price", Matchers.equalTo(ctgMax.get(categories[2])));
    }

    @Test
    void testIsDateTime() {
        String dateTime = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now());
        assert DateTimeUtils.looksLikeDateTime(dateTime):
                "Not a date time "+dateTime;

        DateTimeFormatter.ISO_ZONED_DATE_TIME.format(ZonedDateTime.now());
        assert DateTimeUtils.looksLikeDateTime(dateTime):
                "Not a date time "+dateTime;
    }

    @Test
    void  testContains() {
        String category = UUID.randomUUID().toString();
        String unique = category.substring(0, 8);
        String[] childNames = new String[]{"C_A_" + unique, "C_B_" + unique, "C_C_" + unique};
        ZonedDateTime dateTime = ZonedDateTime.now().withZoneSameLocal(ZoneId.of("Asia/Makassar"));

        Map<String, Float> ctgMax = new HashMap<>();
        Map<String, Integer> ctgCount = new HashMap<>();
        List<String> parentIds = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            String childName = childNames[(i-1)%3];
            float price = i * 10000F;
            ResponseJson<SampleEntityJson> responseJson = given()
                    .body(Json.createObjectBuilder()
                            .add("name", UUID.randomUUID().toString())
                            .add("category", childName)
                            .add("price", price)
                            .add("is_active", (i%3) == 1)
                            .add("x_date", DateTimeUtils.toDateOnly(LocalDate.now()))
                            .add("x_date_time", DateTimeUtils.utcDateDtf.format(dateTime))
                            .build()
                            .toString()
                    )
                    .contentType(MediaType.APPLICATION_JSON.toString())
                    .post("/api/v1/sample-entity")
                    .then()
                    .statusCode(200)
                    .extract().response().getBody().as(new TypeRef<>() {
                    });

            String parentId = responseJson.getData().id;
            parentIds.add(parentId);

            Float max = ctgMax.computeIfAbsent(childName, c -> 0F);
            if (price > max) {
                ctgMax.put(childName, price);
            }

            int count = ctgCount.computeIfAbsent(childName, c -> 0);
            ctgCount.put(childName, count +1);
        }

        Map<String, Integer> parentMapping = new HashMap<>();
        Map<String, List<String>> parentChildMapping = new HashMap<>();
        for (int i = 1; i <= 20; i++) {
            JsonArrayBuilder parentArr = Json.createArrayBuilder();
            for (int j = i-1; j < (i+3); j++) {
                if (j >= 0 && j < parentIds.size()) {
                    String parentId = parentIds.get(j);
                    parentArr.add(Json.createObjectBuilder().add("id", parentId).build());
                    int count = parentMapping.computeIfAbsent(parentId, c -> 0);
                    parentMapping.put(parentId, count+1);
                }
            }

            String createChildPayload = Json.createObjectBuilder()
                    .add("name", UUID.randomUUID().toString())
                    .add("parents", parentArr.build())
                    .build()
                    .toString();
            ResponseJson<SampleChildEntityJson> responseJson = given()
                    .body(createChildPayload)
                    .contentType(MediaType.APPLICATION_JSON.toString())
                    .post("/api/v1/sample-child-entity")
                    .then()
                    .statusCode(200)
                    .extract().response().getBody().as(new TypeRef<>() {
                    });

            for (var parent: responseJson.getData().parents) {
                parentChildMapping.computeIfAbsent(parent.id, p -> new ArrayList<>()).add(responseJson.getData().id);
            }
        }

        given()
                .when().get("/api/v1/sample-child-entity")
                .then().statusCode(200)
                .body("meta.total_data", equalTo(20));

        String testParent = parentIds.get(3);
        given()
                .when().get("/api/v1/sample-child-entity?parents=contains,SampleEntity," + testParent)
                .then().statusCode(200)
                .body("meta.total_data", equalTo(parentMapping.get(testParent)));

        String[] testParents = new String[]{parentIds.get(3), parentIds.get(7)};
        given()
                .when().get("/api/v1/sample-child-entity?parents=contains,SampleEntity," + String.join(",", testParents))
                .then().statusCode(200)
                .body("meta.total_data", equalTo(parentMapping.get(testParents[0]) + parentMapping.get(testParents[1])));

        testParents = new String[]{parentIds.get(3), parentIds.get(4)};
        given()
                .when().get("/api/v1/sample-child-entity?parents=contains,SampleEntity," + String.join(",", testParents))
                .then().statusCode(200)
                .body("meta.total_data", equalTo(5));

        String[] testChildren = new String[]{
                parentChildMapping.get(parentIds.get(3)).getFirst(),
                parentChildMapping.get(parentIds.get(2)).getFirst(),
                parentChildMapping.get(parentIds.get(1)).getFirst(),
                parentChildMapping.get(parentIds.get(0)).getFirst()
        };
        given()
                .when().get("/api/v1/sample-entity?children=contains,SampleChildEntity," + String.join(",", testChildren))
                .then().statusCode(200)
                .body("meta.total_data", equalTo(4));
    }


}
