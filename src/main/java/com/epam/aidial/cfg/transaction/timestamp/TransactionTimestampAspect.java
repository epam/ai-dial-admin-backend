package com.epam.aidial.cfg.transaction.timestamp;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext.TRANSACTION_TIMESTAMP_KEY;

@Aspect
@Component
@RequiredArgsConstructor
public class TransactionTimestampAspect {

    @Before("@annotation(transactional)")
    // todo: adjust to work with nested transactions
    public void initializeTransactionTimestamp(Transactional transactional) {
        if (!TransactionSynchronizationManager.hasResource(TRANSACTION_TIMESTAMP_KEY)) {
            TransactionSynchronizationManager.bindResource(TRANSACTION_TIMESTAMP_KEY, System.currentTimeMillis());

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    TransactionSynchronizationManager.unbindResourceIfPossible(TRANSACTION_TIMESTAMP_KEY);
                }
            });
        }
    }
}