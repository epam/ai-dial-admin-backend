package com.epam.aidial.cfg.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OptimisticLockConflictException extends RuntimeException {

    public OptimisticLockConflictException(String message) {
        super(message);
    }

    public static OptimisticLockConflictException onUpdate(String entityType,
                                                           String entityId,
                                                           String expectedHash,
                                                           String currentHash) {
        return onUpdate(new OptimisticLockConflictExceptionDetails(entityType, entityId, expectedHash, currentHash));
    }

    public static OptimisticLockConflictException onUpdate(OptimisticLockConflictExceptionDetails details) {
        String entityType = details.entityType();
        String entityId = details.entityId();
        String expectedHash = details.expectedHash();
        String currentHash = details.currentHash();

        log.debug("Optimistic lock conflict on update: id={}, expectedHash={}, currentHash={}",
                entityId, expectedHash, currentHash);
        String message = String.format(
                "Unable to update %s '%s'. The data may have been modified by another user, "
                        + "or the name/ID may already exist. Please reload the data and try again.",
                entityType,
                entityId
        );
        return new OptimisticLockConflictException(message);
    }

    public static OptimisticLockConflictException onUpdate(String entityType, String expectedHash, String currentHash) {
        log.debug("Optimistic lock conflict on update: {}, expectedHash={}, currentHash={}",
                entityType, expectedHash, currentHash);
        String message = String.format(
                "Unable to update %s. The data may have been modified by another user. "
                        + "Please reload the data and try again.",
                entityType
        );
        return new OptimisticLockConflictException(message);
    }

    public static OptimisticLockConflictException onUpdate(String resourceType, String resourceId) {
        log.debug("Optimistic lock conflict on update resource: {} '{}'",
                resourceType, resourceId);
        String message = String.format(
                "Unable to update  %s '%s'. The data may have been modified by another user. "
                        + "Please reload the data and try again.",
                resourceType, resourceId);
        return new OptimisticLockConflictException(message);
    }

    public static OptimisticLockConflictException onGetSyncState(String entityType,
                                                                 String entityId,
                                                                 String expectedHash,
                                                                 String currentHash) {
        return onGetSyncState(new OptimisticLockConflictExceptionDetails(entityType, entityId, expectedHash, currentHash));
    }

    public static OptimisticLockConflictException onGetSyncState(OptimisticLockConflictExceptionDetails details) {
        String entityType = details.entityType();
        String entityId = details.entityId();
        String expectedHash = details.expectedHash();
        String currentHash = details.currentHash();

        log.debug("Optimistic lock conflict on get sync state: id={}, expectedHash={}, currentHash={}",
                entityId, expectedHash, currentHash);
        String message = String.format(
                "Unable to get sync state for %s '%s'. The data may have been modified by another user. "
                        + "Please reload the data and try again.",
                entityType,
                entityId
        );
        return new OptimisticLockConflictException(message);
    }

    public record OptimisticLockConflictExceptionDetails(String entityType,
                                                         String entityId,
                                                         String expectedHash,
                                                         String currentHash) {
    }
}