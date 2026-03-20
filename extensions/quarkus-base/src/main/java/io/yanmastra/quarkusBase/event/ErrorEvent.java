package io.yanmastra.quarkusBase.event;

import java.time.Instant;

public class ErrorEvent {
    private final Exception exception;
    private final int statusCode;
    private final String path;
    private final String message;
    private final Instant timestamp;

    public ErrorEvent(Exception exception, int statusCode, String path, String message) {
        this.exception = exception;
        this.statusCode = statusCode;
        this.path = path;
        this.message = message;
        this.timestamp = Instant.now();
    }

    public Exception getException() {
        return exception;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getPath() {
        return path;
    }

    public String getMessage() {
        return message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
