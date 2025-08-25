package com.epam.aidial.cfg.exception;

public class ConcurrencyModificationException extends RuntimeException {
    public ConcurrencyModificationException(String message) {
        super(message);
    }
}
