package io.yanmastra.errormail;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.yanmastra.quarkusBase.event.ErrorEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class ErrorMailNotifier {

    private static final Logger log = Logger.getLogger(ErrorMailNotifier.class);

    @Inject
    ReactiveMailer mailer;

    @Inject
    ErrorMailConfig config;

    @Inject
    CriticalErrorFilter filter;

    private final Map<String, Instant> lastSentMap = new ConcurrentHashMap<>();

    void onError(@Observes ErrorEvent event) {
        if (!config.enabled()) {
            return;
        }

        if (config.recipients().isEmpty()) {
            log.warn("Error mail notification is enabled but no recipients are configured. Set 'error-mail-notification.recipients'.");
            return;
        }

        if (!filter.isCritical(event)) {
            return;
        }

        String errorKey = buildErrorKey(event);
        if (isInCooldown(errorKey)) {
            log.debugv("Skipping error mail for '{0}' — still in cooldown period", errorKey);
            return;
        }

        sendErrorMail(event);
        lastSentMap.put(errorKey, Instant.now());
    }

    private boolean isInCooldown(String errorKey) {
        Instant lastSent = lastSentMap.get(errorKey);
        if (lastSent == null) {
            return false;
        }
        return Instant.now().isBefore(lastSent.plusSeconds(config.cooldownSeconds()));
    }

    private String buildErrorKey(ErrorEvent event) {
        String exceptionClass = event.getException().getClass().getName();
        return exceptionClass + ":" + event.getStatusCode() + ":" + event.getPath();
    }

    private void sendErrorMail(ErrorEvent event) {
        try {
            String subject = buildSubject(event);
            String body = buildBody(event);

            Mail mail = new Mail()
                    .setSubject(subject)
                    .setHtml(body)
                    .setFrom(config.from());

            for (String recipient : config.recipients()) {
                mail.addTo(recipient);
            }

            mailer.send(mail)
                    .subscribe().with(
                            success -> log.infov("Critical error notification sent to {0}", config.recipients()),
                            failure -> log.errorv("Failed to send critical error notification: {0}", failure.getMessage())
                    );
        } catch (Exception e) {
            log.error("Failed to prepare error notification email", e);
        }
    }

    private String buildSubject(ErrorEvent event) {
        String appName = config.appName().orElse("Application");
        String exceptionName = event.getException().getClass().getSimpleName();
        return String.format("%s %s - %s on %s", config.subjectPrefix(), appName, exceptionName, event.getPath());
    }

    private String buildBody(ErrorEvent event) {
        String timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
                .withZone(ZoneId.systemDefault())
                .format(event.getTimestamp());

        String appName = config.appName().orElse("Application");
        String stackTrace = getStackTrace(event.getException());

        return """
                <html>
                <body style="font-family: Arial, sans-serif; color: #333;">
                    <h2 style="color: #d32f2f;">Critical Error Notification</h2>
                    <table style="border-collapse: collapse; width: 100%%;">
                        <tr>
                            <td style="padding: 8px; border: 1px solid #ddd; font-weight: bold;">Application</td>
                            <td style="padding: 8px; border: 1px solid #ddd;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 8px; border: 1px solid #ddd; font-weight: bold;">Timestamp</td>
                            <td style="padding: 8px; border: 1px solid #ddd;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 8px; border: 1px solid #ddd; font-weight: bold;">Status Code</td>
                            <td style="padding: 8px; border: 1px solid #ddd;">%d</td>
                        </tr>
                        <tr>
                            <td style="padding: 8px; border: 1px solid #ddd; font-weight: bold;">Path</td>
                            <td style="padding: 8px; border: 1px solid #ddd;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 8px; border: 1px solid #ddd; font-weight: bold;">Exception</td>
                            <td style="padding: 8px; border: 1px solid #ddd;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 8px; border: 1px solid #ddd; font-weight: bold;">Message</td>
                            <td style="padding: 8px; border: 1px solid #ddd;">%s</td>
                        </tr>
                    </table>
                    <h3>Stack Trace</h3>
                    <pre style="background: #f5f5f5; padding: 12px; border-radius: 4px; overflow-x: auto; font-size: 12px;">%s</pre>
                </body>
                </html>
                """.formatted(
                appName,
                timestamp,
                event.getStatusCode(),
                event.getPath(),
                event.getException().getClass().getName(),
                event.getMessage() != null ? event.getMessage() : "N/A",
                stackTrace
        );
    }

    private String getStackTrace(Exception exception) {
        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        String trace = sw.toString();
        if (trace.length() > 5000) {
            trace = trace.substring(0, 5000) + "\n... (truncated)";
        }
        return trace;
    }
}
