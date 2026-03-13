package com.epam.aidial.cfg.dao.listener;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.model.RoleLimitEntity;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@LogExecution
public class RoleLimitEntityListener {

    private final TransactionTimestampContext transactionTimestampContext;

    @PrePersist
    public void prePersist(RoleLimitEntity entity) {
        handleEntity(entity);
    }

    @PreUpdate
    public void preUpdate(RoleLimitEntity entity) {
        handleEntity(entity);
    }

    @PreRemove
    public void preRemove(RoleLimitEntity entity) {
        handleEntity(entity);
    }

    private void handleEntity(RoleLimitEntity entity) {
        entity.getRole().setUpdatedAt(transactionTimestampContext.getTimestamp());
        entity.getDeployment().getOwner().setUpdatedAt(transactionTimestampContext.getTimestamp());
    }
}
