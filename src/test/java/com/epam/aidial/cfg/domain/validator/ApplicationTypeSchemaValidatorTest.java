package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class ApplicationTypeSchemaValidatorTest {

    private static final String ID_VALIDATION_PATTERN = "^[a-zA-Z0-9-_.]{1,30}$";

    @Mock
    private IdFieldValidator idFieldValidator;
    @Mock
    private RouteValidator routeValidator;
    @Mock
    private DisplayFieldsValidator displayFieldsValidator;

    private ApplicationTypeSchemaValidator applicationTypeSchemaValidator;

    @BeforeEach
    void setUp() {
        applicationTypeSchemaValidator = new ApplicationTypeSchemaValidator(idFieldValidator, routeValidator, displayFieldsValidator, null);
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

    @ParameterizedTest
    @ValueSource(strings = {"valid-id", "valid_id", "ValidId123", "id-123_456", "id.with.dots"})
    void validateCreation_shouldNotThrowExceptionForValidId(String id) {
        // given
        ReflectionTestUtils.setField(applicationTypeSchemaValidator, "applicationTypeSchemaIdValidationPattern", ID_VALIDATION_PATTERN);

        ApplicationTypeSchema schema = new ApplicationTypeSchema();
        schema.setSchemaId(id);

        // when/then
        assertThatNoException().isThrownBy(() -> applicationTypeSchemaValidator.validateCreation(schema));
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid id with spaces", "invalid@id", "invalid#id", "invalid$id",
            "id-that-is-way-too-long-for-validation-pattern"})
    void validateCreation_shouldThrowExceptionForInvalidId(String id) {
        // given
        ReflectionTestUtils.setField(applicationTypeSchemaValidator, "applicationTypeSchemaIdValidationPattern", ID_VALIDATION_PATTERN);

        ApplicationTypeSchema schema = new ApplicationTypeSchema();
        schema.setSchemaId(id);

        // when/then
        assertThatThrownBy(() -> applicationTypeSchemaValidator.validateCreation(schema))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not match the required pattern");
    }

    @Test
    void validateCreation_shouldThrowExceptionWhenIdFieldValidatorThrows() {
        // given
        ApplicationTypeSchema applicationTypeSchema = new ApplicationTypeSchema();
        applicationTypeSchema.setSchemaId("schema_id");

        doThrow(IllegalArgumentException.class).when(idFieldValidator)
                .validateId("ApplicationTypeSchema", "schema_id", "schemaId");

        // when/then
        Assertions.assertThatThrownBy(() -> applicationTypeSchemaValidator.validateCreation(applicationTypeSchema))
                .isInstanceOf(IllegalArgumentException.class);
    }

}