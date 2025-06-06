package com.epam.aidial.cfg.exception;

/**
 * Created by Aliaksei Kurnosau on 9/11/24.
 */
public class EntityNotFoundException extends ValidationException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}
