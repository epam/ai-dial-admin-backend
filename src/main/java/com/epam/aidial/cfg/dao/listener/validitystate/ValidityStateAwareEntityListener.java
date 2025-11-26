package com.epam.aidial.cfg.dao.listener.validitystate;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.listener.validitystate.setter.ApplicationValidityStateSetter;
import com.epam.aidial.cfg.dao.listener.validitystate.setter.KeyValidityStateSetter;
import com.epam.aidial.cfg.dao.model.ApplicationEntity;
import com.epam.aidial.cfg.dao.model.KeyEntity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@LogExecution
public class ValidityStateAwareEntityListener {

    private final KeyValidityStateSetter keyValidityStateSetter;
    private final ApplicationValidityStateSetter applicationValidityStateSetter;

    @PrePersist
    public void prePersist(Object entity) {
        handleEntity(entity);
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        handleEntity(entity);
    }

    private void handleEntity(Object entity) {
        if (entity instanceof KeyEntity keyEntity) {
            keyValidityStateSetter.setValidityState(keyEntity);
        } else if (entity instanceof ApplicationEntity applicationEntity) {
            applicationValidityStateSetter.setValidityState(applicationEntity);
        }
    }
}
