package io.yanmastra.errormail;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

import java.util.Optional;
import java.util.Set;

@ConfigMapping(prefix = "error-mail-notification")
public interface ErrorMailConfig {

    /**
     * Enable or disable error email notifications.
     */
    @WithName("enabled")
    @WithDefault("true")
    boolean enabled();

    /**
     * Email addresses to send critical error notifications to.
     * Comma-separated list, e.g. "admin@example.com,ops@example.com"
     */
    @WithName("recipients")
    Set<String> recipients();

    /**
     * The sender email address.
     */
    @WithName("from")
    @WithDefault("noreply@localhost")
    String from();

    /**
     * Subject prefix for error notification emails.
     */
    @WithName("subject-prefix")
    @WithDefault("[CRITICAL ERROR]")
    String subjectPrefix();

    /**
     * Application name included in the email for identification.
     */
    @WithName("app-name")
    Optional<String> appName();

    /**
     * Cooldown period in seconds between sending the same error type.
     * Prevents email flooding when the same error occurs repeatedly.
     */
    @WithName("cooldown-seconds")
    @WithDefault("300")
    int cooldownSeconds();
}
