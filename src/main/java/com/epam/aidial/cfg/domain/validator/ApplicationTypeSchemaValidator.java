package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ApplicationTypeSchemaValidator {

    public void validateUpdate(String schemaId, ApplicationTypeSchema applicationTypeSchema) {
        if (!Objects.equals(schemaId, applicationTypeSchema.getSchemaId())) {
            throw new IllegalArgumentException("Schema id can not be updated for application type schema "
                    + "with schema id: '" + schemaId + "'. New schema id: '" + applicationTypeSchema.getSchemaId() + "'");
        }
    }
}
