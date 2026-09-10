package com.epam.aidial.cfg.dao.audit.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class AuditParentActivityHolder {

    private static final Object PARENT_ACTIVITY_TRANSACTION_KEY = new Object();

    public Scope openScope(UUID parentId) {

        final Object previousTransactionResource;

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            previousTransactionResource =
                    TransactionSynchronizationManager.unbindResourceIfPossible(PARENT_ACTIVITY_TRANSACTION_KEY);

            TransactionSynchronizationManager.bindResource(PARENT_ACTIVITY_TRANSACTION_KEY, parentId);
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void beforeCommit(boolean readOnly) {
                    TransactionSynchronizationManager.bindResource(PARENT_ACTIVITY_TRANSACTION_KEY, parentId);
                }

                @Override
                public void afterCompletion(int status) {
                    if (previousTransactionResource != null) {
                        TransactionSynchronizationManager.bindResource(
                                PARENT_ACTIVITY_TRANSACTION_KEY,
                                previousTransactionResource
                        );
                    } else {
                        TransactionSynchronizationManager.unbindResourceIfPossible(
                                PARENT_ACTIVITY_TRANSACTION_KEY
                        );
                    }
                }
            });

        } else {
            previousTransactionResource = null;
        }

        return () -> {
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.unbindResourceIfPossible(PARENT_ACTIVITY_TRANSACTION_KEY);

                if (previousTransactionResource != null) {
                    TransactionSynchronizationManager.bindResource(
                            PARENT_ACTIVITY_TRANSACTION_KEY,
                            previousTransactionResource
                    );
                }
            }
        };
    }


    public Optional<UUID> getParentActivityId() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            Object bound = TransactionSynchronizationManager.getResource(PARENT_ACTIVITY_TRANSACTION_KEY);
            if (bound instanceof UUID uuid) {
                return Optional.of(uuid);
            }
        }
        return Optional.empty();
    }

    public interface Scope extends AutoCloseable {
        @Override
        void close();
    }
}