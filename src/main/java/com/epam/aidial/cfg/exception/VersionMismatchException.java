package com.epam.aidial.cfg.exception;

/**
 * Created by Aliaksei Kurnosau on 9/9/24.
 */
public class VersionMismatchException extends ValidationException {
    public VersionMismatchException(String message) {
        super(message);
    }
}
