package com.epam.aidial.cfg.dao.listener.validitystate.setter;

import com.epam.aidial.cfg.dao.listener.validitystate.resolver.ApplicationValidityStateResolver;
import com.epam.aidial.cfg.dao.model.ApplicationEntity;
import com.epam.aidial.cfg.dao.model.ValidityStateEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationValidityStateSetter {

    private final ApplicationValidityStateResolver applicationValidityStateResolver;

    public void setValidityState(ApplicationEntity entity) {
        ValidityStateEntity validityStateEntity = applicationValidityStateResolver.resolveValidityState(entity);
        entity.setValidityState(validityStateEntity);
    }
}
