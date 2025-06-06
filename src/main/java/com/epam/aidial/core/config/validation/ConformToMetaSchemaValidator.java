package com.epam.aidial.core.config.validation;

import java.util.Map;

import com.epam.aidial.core.metaschemas.MetaSchemaHolder;

import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ConformToMetaSchemaValidator implements ConstraintValidator<ConformToMetaSchema, Map<String, String>> {

    private static final JsonSchemaFactory SCHEMA_FACTORY = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
    private static final JsonSchema SCHEMA = SCHEMA_FACTORY.getSchema(MetaSchemaHolder.getCustomApplicationMetaSchema());

    @Override
    public boolean isValid(Map<String, String> idSchemaMap, ConstraintValidatorContext context) {
        if (idSchemaMap == null) {
            return true;
        }
        for (Map.Entry<String, String> entry : idSchemaMap.entrySet()) {
            if (!SCHEMA.validate(entry.getValue(), InputFormat.JSON).isEmpty()) {
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