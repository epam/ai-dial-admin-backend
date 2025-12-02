package com.epam.aidial.cfg.domain.resolver.key;

import com.epam.aidial.cfg.dao.model.KeyEntity;
import com.epam.aidial.cfg.dao.model.ValidityStateEntity;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KeyValidityStateOnGetResolver {

    private final TransactionTimestampContext transactionTimestampContext;

    public ValidityStateEntity resolveValidityStateEntity(KeyEntity keyEntity) {
        long now = transactionTimestampContext.getTimestamp();
        boolean isKeyNotExpired = keyEntity.getExpiresAt() == null || keyEntity.getExpiresAt() >= now;

        if (isKeyNotExpired) {
            return keyEntity.getValidityState();
        }

        ValidityStateEntity validityStateEntity = keyEntity.getValidityState();
        ValidityStateEntity newValidityStateEntity = new ValidityStateEntity();

        if (validityStateEntity.isValid()) {
            newValidityStateEntity.setMessage("Key is expired");
            newValidityStateEntity.setValid(false);
        } else {
            newValidityStateEntity.setMessage(validityStateEntity.getMessage() + ", Key is expired");
        }

        return newValidityStateEntity;
    }
}
