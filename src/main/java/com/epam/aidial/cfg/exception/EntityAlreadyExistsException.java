package com.epam.aidial.cfg.exception;

/**
 * Created by Aliaksei Kurnosau on 9/13/24.
 */
public class EntityAlreadyExistsException extends ValidationException {
    public EntityAlreadyExistsException(String message) {
        super(message);
    }
}
