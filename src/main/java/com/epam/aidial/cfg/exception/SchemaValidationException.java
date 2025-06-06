package com.epam.aidial.cfg.exception;

public class SchemaValidationException extends RuntimeException {

    public SchemaValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}