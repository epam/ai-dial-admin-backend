package com.epam.aidial.cfg.exception;

public class OptimisticLockConflictException extends RuntimeException {

    public OptimisticLockConflictException(String message) {
        super(message);
    }
}
