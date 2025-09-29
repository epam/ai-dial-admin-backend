package com.epam.aidial.cfg.dao.listener.validitystate.resolver;

import com.epam.aidial.cfg.dao.model.KeyEntity;
import com.epam.aidial.cfg.dao.model.ValidityStateEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

@Component
public class KeyValidityStateResolver {

    public ValidityStateEntity resolveValidityState(KeyEntity entity) {
        String validityStateMessage = CollectionUtils.isEmpty(entity.getRoles()) ? "No roles assigned" : null;

        ValidityStateEntity validityStateEntity = new ValidityStateEntity();
        validityStateEntity.setMessage(validityStateMessage);
        validityStateEntity.setValid(validityStateMessage == null);
        return validityStateEntity;
    }
}
