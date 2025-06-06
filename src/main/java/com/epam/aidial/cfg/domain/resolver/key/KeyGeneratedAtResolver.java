package com.epam.aidial.cfg.domain.resolver.key;

import com.epam.aidial.cfg.dao.model.KeyEntity;
import com.epam.aidial.cfg.domain.model.Key;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class KeyGeneratedAtResolver {

    private final TransactionTimestampContext transactionTimestampContext;

    public long resolveKeyGeneratedAtValueDuringCreation() {
        return transactionTimestampContext.getTimestamp();
    }

    public long resolveKeyGeneratedAtValueDuringUpdate(Key key, KeyEntity existingEntity) {
        return !Objects.equals(key.getKey(), existingEntity.getKey())
                ? transactionTimestampContext.getTimestamp()
                : existingEntity.getKeyGeneratedAt();
    }
}
