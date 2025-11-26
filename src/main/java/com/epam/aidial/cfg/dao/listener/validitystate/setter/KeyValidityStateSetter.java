package com.epam.aidial.cfg.dao.listener.validitystate.setter;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.listener.validitystate.resolver.KeyValidityStateResolver;
import com.epam.aidial.cfg.dao.model.KeyEntity;
import com.epam.aidial.cfg.dao.model.ValidityStateEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@LogExecution
public class KeyValidityStateSetter {

    private final KeyValidityStateResolver keyValidityStateResolver;

    public void setValidityState(KeyEntity entity) {
        ValidityStateEntity validityStateEntity = keyValidityStateResolver.resolveValidityState(entity);
        entity.setValidityState(validityStateEntity);
    }
}
