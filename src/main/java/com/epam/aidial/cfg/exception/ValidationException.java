package com.epam.aidial.cfg.exception;

/**
 * Created by Aliaksei Kurnosau on 9/26/24.
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
