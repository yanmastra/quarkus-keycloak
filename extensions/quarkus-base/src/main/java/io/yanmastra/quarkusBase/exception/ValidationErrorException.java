package io.yanmastra.quarkusBase.exception;

import java.util.Map;

public class ValidationErrorException extends RuntimeException {
    private final Map<String, String> errors;
    public ValidationErrorException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors;
    }
    public ValidationErrorException(String message, Map<String, String> errors, Throwable cause) {
        super(message, cause);
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}

