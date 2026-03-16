package com.epam.aidial.cfg.dao.listener;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.model.DeploymentEntity;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import jakarta.persistence.PreUpdate;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@LogExecution
public class DeploymentEntityListener {

    private final TransactionTimestampContext transactionTimestampContext;

    @PreUpdate
    public void preUpdate(DeploymentEntity entity) {
        entity.getOwner().setUpdatedAt(transactionTimestampContext.getTimestamp());
    }
}
