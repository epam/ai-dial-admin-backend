package com.epam.aidial.core.config.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Map;

public class ConformToMetaSchemaValidator implements ConstraintValidator<ConformToMetaSchema, Map<String, String>> {

    @Override
    public boolean isValid(Map<String, String> idSchemaMap, ConstraintValidatorContext context) {
        if (idSchemaMap == null) {
            return true;
        }
        for (Map.Entry<String, String> entry : idSchemaMap.entrySet()) {
            if (!SchemaConformToMetaSchemaValidator.isValid(entry.getValue())) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                        .addBeanNode()
                        .inContainer(Map.class, 1)
                        .inIterable().atKey(entry.getKey())
                        .addConstraintViolation();
                return false;
            }
        }
        return true;
    }
}