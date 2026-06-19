package io.yanmastra.quarkus.mediafilemanager.it;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class QuarkusMediaFileManagerResourceTest {

    @Test
    public void testUploadImage() throws IOException {
        File img = createTempPng();

        Response response = given()
                .multiPart("file", img, "image/png")
                .when()
                .post("/test/upload/image")
                .then()
                .statusCode(200)
                .body("file_id", notNullValue())
                .body("default_file", notNullValue())
                .body("w320", notNullValue())
                .body("w640", notNullValue())
                .extract().response();

        String fileId = response.jsonPath().getString("file_id");
        String defaultFile = response.jsonPath().getString("default_file");

        assertNotNull(fileId);
        assertTrue(defaultFile.startsWith("/media/"));

        // verify the file is retrievable
        given()
                .when()
                .get(defaultFile)
                .then()
                .statusCode(200)
                .contentType(containsString("image/png"));

        // cleanup
        given().delete("/test/media/" + fileId).then().statusCode(204);
    }

    @Test
    public void testUploadImageWithLocation() throws IOException {
        File img = createTempPng();

        // intentionally messy location with leading/trailing slashes
        Response response = given()
                .multiPart("file", img, "image/png")
                .multiPart("location", "/mastra/wayan/")
                .when()
                .post("/test/upload/image")
                .then()
                .statusCode(200)
                .body("file_id", notNullValue())
                .body("default_file", containsString("/media/mastra/wayan/"))
                .body("w320", containsString("/media/mastra/wayan/w320/"))
                .extract().response();

        String fileId = response.jsonPath().getString("file_id");
        String defaultFile = response.jsonPath().getString("default_file");

        // verify the file is retrievable via the returned URL (path depth > 1)
        given()
                .when()
                .get(defaultFile)
                .then()
                .statusCode(200)
                .contentType(containsString("image/png"));

        given().delete("/test/media/" + fileId).then().statusCode(204);
    }

    @Test
    public void testUploadSecuredImage() throws IOException {
        File img = createTempPng();

        Response response = given()
                .multiPart("file", img, "image/png")
                .when()
                .post("/test/upload/image/secured")
                .then()
                .statusCode(200)
                .body("file_id", notNullValue())
                .body("default_file", containsString("/secured-media/"))
                .extract().response();

        String fileId = response.jsonPath().getString("file_id");
        given().delete("/test/media/secured/" + fileId).then().statusCode(204);
    }

    @Test
    public void testUploadFile() throws IOException {
        File txt = File.createTempFile("test-", ".txt");
        txt.deleteOnExit();
        Files.writeString(txt.toPath(), "hello world");

        Response response = given()
                .multiPart("file", txt, "text/plain")
                .when()
                .post("/test/upload/file")
                .then()
                .statusCode(200)
                .body("file_id", notNullValue())
                .body("default_file", notNullValue())
                .extract().response();

        String fileId = response.jsonPath().getString("file_id");
        given().delete("/test/media/" + fileId).then().statusCode(204);
    }

    @Test
    public void testLoadByFileId() throws IOException {
        File img = createTempPng();

        // upload with a deep specificLocation
        Response uploadResponse = given()
                .multiPart("file", img, "image/png")
                .multiPart("location", "/products/electronics/")
                .when()
                .post("/test/upload/image")
                .then()
                .statusCode(200)
                .body("file_id", notNullValue())
                .extract().response();

        String fileId = uploadResponse.jsonPath().getString("file_id");

        // load original by fileId only — no need to know the full path
        given()
                .when()
                .get("/media/id/" + fileId)
                .then()
                .statusCode(200)
                .contentType(containsString("image/png"));

        // load w320 variant by fileId + size
        given()
                .when()
                .get("/media/id/" + fileId + "/w320")
                .then()
                .statusCode(200)
                .contentType(containsString("image/png"));

        // unknown size → 404
        given()
                .when()
                .get("/media/id/" + fileId + "/w9999")
                .then()
                .statusCode(404);

        given().delete("/test/media/" + fileId).then().statusCode(204);
    }

    private File createTempPng() throws IOException {
        File tmp = File.createTempFile("test-", ".png");
        tmp.deleteOnExit();
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(img, "png", tmp);
        return tmp;
    }
}
