package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.dao.model.KeyEntity;
import com.epam.aidial.cfg.domain.model.Key;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeyValidator {

    private final TransactionTimestampContext transactionTimestampContext;

    @Value("${validation.key.name:}")
    private String keyNameValidationPattern;

    public void validateCreation(Key key) {
        validateKeyName(key);
        
        long now = transactionTimestampContext.getTimestamp();
        Long expiresAt = key.getExpiresAt();
        if (expiresAt != null && expiresAt <= now) {
            throw new IllegalArgumentException("Key expiresAt ms: '" + expiresAt + "' should be greater than current epoch ms: '" + now + "'");
        }
    }

    private void validateKeyName(Key key) {
        final String keyName = key.getName();

        if (StringUtils.isEmpty(keyNameValidationPattern)) {
            log.debug("Key name validation pattern is empty, skipping validation for key: {}", keyName);
            return;
        }

        if (!Pattern.matches(keyNameValidationPattern, keyName)) {
            throw new IllegalArgumentException("Key name '" + keyName
                + "' does not match the required pattern: " + keyNameValidationPattern);
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
