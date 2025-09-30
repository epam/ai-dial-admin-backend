package com.epam.aidial.cfg.dao.listener.validitystate.setter;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.listener.validitystate.resolver.ApplicationValidityStateResolver;
import com.epam.aidial.cfg.dao.model.ApplicationEntity;
import com.epam.aidial.cfg.dao.model.ValidityStateEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@LogExecution
public class ApplicationValidityStateSetter {

    private final ApplicationValidityStateResolver applicationValidityStateResolver;

    public void setValidityState(ApplicationEntity entity) {
        ValidityStateEntity validityStateEntity = applicationValidityStateResolver.resolveValidityState(entity);
        entity.setValidityState(validityStateEntity);
    }
}
