package com.epam.aidial.cfg.dao.listener.validitystate.resolver;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.model.KeyEntity;
import com.epam.aidial.cfg.dao.model.ValidityStateEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@LogExecution
public class KeyValidityStateResolver {

    public ValidityStateEntity resolveValidityState(KeyEntity entity) {
        ValidityStateEntity validityStateEntity = new ValidityStateEntity();

        List<String> validationMessages = getValidationMessages(entity);

        if (!validationMessages.isEmpty()) {
            String validityStateMessage = String.join(", ", validationMessages);
            validityStateEntity.setMessage(validityStateMessage);
            validityStateEntity.setValid(false);
        } else {
            validityStateEntity.setValid(true);
        }

        return validityStateEntity;
    }

    private List<String> getValidationMessages(KeyEntity entity) {
        List<String> errors = new ArrayList<>(2);

        if (CollectionUtils.isEmpty(entity.getRoles())) {
            errors.add("No roles assigned");
        }

        if (StringUtils.isBlank(entity.getKey())) {
            errors.add("Key value is missing");
        }

        return errors;
    }
}
