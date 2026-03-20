package io.yanmastra.errormail.it;

import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ErrorMailNotificationTest {

    @Inject
    Logger log;

    @Inject
    MockMailbox mockMailbox;

    @BeforeEach
    void setup() {
        mockMailbox.clear();
    }

    @Test
    @Order(1)
    void testOkEndpointNoEmail() {
        given()
                .accept("application/json")
                .when().get("/test/ok")
                .then()
                .statusCode(200);

        List<io.quarkus.mailer.Mail> sent = mockMailbox.getMailsSentTo("test-admin@example.com");
        assertTrue(sent.isEmpty(), "No email should be sent for successful requests");
    }

    @Test
    @Order(2)
    void testServerErrorTriggersEmail() {
        given()
                .accept("application/json")
                .when().get("/test/server-error")
                .then()
                .statusCode(500)
                .body("success", is(false));

        List<io.quarkus.mailer.Mail> sent = mockMailbox.getMailsSentTo("test-admin@example.com");
        assertEquals(1, sent.size(), "One email should be sent for a 500 error");

        io.quarkus.mailer.Mail mail = sent.get(0);
        log.infov("Email subject: {0}", mail.getSubject());
        assertTrue(mail.getSubject().contains("[TEST ERROR]"), "Subject should contain the configured prefix");
        assertTrue(mail.getSubject().contains("integration-test-app"), "Subject should contain app name");
        assertTrue(mail.getSubject().contains("RuntimeException"), "Subject should contain exception name");

        String html = mail.getHtml();
        assertNotNull(html, "Email should have HTML body");
        assertTrue(html.contains("Simulated internal server error"), "Body should contain the error message");
        assertTrue(html.contains("/test/server-error"), "Body should contain the request path");
        assertTrue(html.contains("500"), "Body should contain the status code");

        List<io.quarkus.mailer.Mail> sentToOps = mockMailbox.getMailsSentTo("test-ops@example.com");
        assertEquals(1, sentToOps.size(), "Email should also be sent to second recipient");
    }

    @Test
    @Order(3)
    void testClientErrorDoesNotTriggerEmail() {
        given()
                .accept("application/json")
                .when().get("/test/client-error")
                .then()
                .statusCode(400)
                .body("success", is(false));

        List<io.quarkus.mailer.Mail> sent = mockMailbox.getMailsSentTo("test-admin@example.com");
        assertTrue(sent.isEmpty(), "No email should be sent for 400 errors (not critical by default)");
    }

    @Test
    @Order(4)
    void testForbiddenDoesNotTriggerEmail() {
        given()
                .accept("application/json")
                .when().get("/test/forbidden")
                .then()
                .statusCode(403)
                .body("success", is(false));

        List<io.quarkus.mailer.Mail> sent = mockMailbox.getMailsSentTo("test-admin@example.com");
        assertTrue(sent.isEmpty(), "No email should be sent for 403 errors (not critical by default)");
    }

    @Test
    @Order(5)
    void testCooldownPreventsFlood() throws InterruptedException {
        // Uses dedicated endpoints that are not hit by other tests,
        // so the cooldown map is clean for these error keys.

        // First request — should trigger email
        given()
                .accept("application/json")
                .when().get("/test/cooldown-error")
                .then()
                .statusCode(500);

        List<io.quarkus.mailer.Mail> sentFirst = mockMailbox.getMailsSentTo("test-admin@example.com");
        assertEquals(1, sentFirst.size(), "First error should trigger an email");

        // Second request (same error) — should be in cooldown
        given()
                .accept("application/json")
                .when().get("/test/cooldown-error")
                .then()
                .statusCode(500);

        List<io.quarkus.mailer.Mail> sentSecond = mockMailbox.getMailsSentTo("test-admin@example.com");
        assertEquals(1, sentSecond.size(), "Second identical error should be suppressed by cooldown");

        // Different error — should still trigger email (different key)
        given()
                .accept("application/json")
                .when().get("/test/cooldown-error-different")
                .then()
                .statusCode(500);

        List<io.quarkus.mailer.Mail> sentThird = mockMailbox.getMailsSentTo("test-admin@example.com");
        assertEquals(2, sentThird.size(), "Different error type should trigger a separate email");
    }
}
