package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.dao.model.KeyEntity;
import com.epam.aidial.cfg.domain.model.Key;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class KeyValidator {

    private final TransactionTimestampContext transactionTimestampContext;

    public void validateCreation(Key key) {
        long now = transactionTimestampContext.getTimestamp();
        Long expiresAt = key.getExpiresAt();
        if (expiresAt != null && expiresAt <= now) {
            throw new IllegalArgumentException("Key expiresAt ms: '" + expiresAt + "' should be greater than current epoch ms: '" + now + "'");
        }
    }

    public void validateUpdate(String keyName, Key key, KeyEntity existingEntity) {
        if (!Objects.equals(keyName, key.getName())) {
            throw new IllegalArgumentException("Key with name: '" + keyName + "' can not be renamed. New key name: '" + key.getName() + "'");
        }

        Long expiresAt = key.getExpiresAt();
        long createdAt = existingEntity.getCreatedAt();
        if (expiresAt != null && expiresAt <= createdAt) {
            throw new IllegalArgumentException("Key expiresAt ms: '" + expiresAt + "' should be greater than key createdAt ms: '" + createdAt + "'");
        }
    }
}
