package com.epam.aidial.cfg.transaction.timestamp;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class TransactionTimestampContext {

    static final String TRANSACTION_TIMESTAMP_KEY = "TRANSACTION_TIMESTAMP_KEY";

    public long getTimestamp() {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("No active transaction");
        }

        Long timestamp = (Long) TransactionSynchronizationManager.getResource(TRANSACTION_TIMESTAMP_KEY);
        if (timestamp == null) {
            throw new IllegalStateException("Timestamp not initialized");
        }

        return timestamp;
    }
}
