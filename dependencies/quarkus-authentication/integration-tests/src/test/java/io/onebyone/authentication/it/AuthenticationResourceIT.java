package io.onebyone.authentication.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.onebyone.authentication.utils.JsonUtils;
import io.quarkus.test.Mock;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;

@QuarkusIntegrationTest
public class AuthenticationResourceIT extends AuthenticationResourceTest {
}
