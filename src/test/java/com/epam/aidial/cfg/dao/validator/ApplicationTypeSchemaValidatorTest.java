package com.epam.aidial.cfg.dao.validator;

import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import com.epam.aidial.cfg.domain.validator.ApplicationTypeSchemaValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApplicationTypeSchemaValidatorTest {

    private ApplicationTypeSchemaValidator applicationTypeSchemaValidator;

    @BeforeEach
    void setUp() {
        applicationTypeSchemaValidator = new ApplicationTypeSchemaValidator();
    }

    @Test
    void validateUpdate_shouldThrowExceptionWhenApplicationTypeSchemaIdIsUpdated() {
        ApplicationTypeSchema applicationTypeSchema = new ApplicationTypeSchema();
        applicationTypeSchema.setSchemaId("new_schema_id");

        assertThatThrownBy(() -> applicationTypeSchemaValidator.validateUpdate("schema_id", applicationTypeSchema))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Schema id can not be updated for application type schema with schema id: 'schema_id'. New schema id: 'new_schema_id'");
    }

    @Test
    void validateUpdate_shouldDoNothingWhenApplicationTypeSchemaIdIsNotUpdated() {
        ApplicationTypeSchema applicationTypeSchema = new ApplicationTypeSchema();
        applicationTypeSchema.setSchemaId("schema_id");

        assertThatNoException().isThrownBy(() -> applicationTypeSchemaValidator.validateUpdate("schema_id", applicationTypeSchema));
    }

}