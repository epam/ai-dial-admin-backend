package com.epam.aidial.cfg.domain.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DisplayFieldsValidatorTest {

    private DisplayFieldsValidator displayFieldsValidator;

    @BeforeEach
    void setUp() {
        displayFieldsValidator = new DisplayFieldsValidator();
    }

    @ParameterizedTest
    @CsvSource({"''", "' '"})
    void validateDisplayNameDisplayVersion_shouldThrowExceptionWhenDisplayNameIsBlank(String displayName) {
        assertThatThrownBy(() -> displayFieldsValidator.validateDisplayNameDisplayVersion(displayName, null, "DomainObjectType", "name"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Display name: '" + displayName + "'" + " must not be blank for DomainObjectType with id:'name'");

    }

    @ParameterizedTest
    @CsvSource({"''", "' '"})
    void validateDisplayNameDisplayVersion_shouldThrowExceptionWhenDisplayVersionIsBlank(String displayVersion) {
        assertThatThrownBy(() -> displayFieldsValidator.validateDisplayNameDisplayVersion("test", displayVersion, "DomainObjectType", "name"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Display version: '" + displayVersion + "'" + " must not be blank for DomainObjectType with id:'name'");
    }

    @ParameterizedTest
    @CsvSource(value = {"null", "''", "' '"}, nullValues = "null")
    void validateDisplayName_shouldThrowExceptionWhenDisplayNameIsNullOrBlank(String displayName) {
        assertThatThrownBy(() -> displayFieldsValidator.validateDisplayName(displayName, "DomainObjectType", "name"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Display name: '" + displayName + "'" + " must not be blank for DomainObjectType with id:'name'");
    }

    @Test
    void validateDisplayNameDisplayVersion_shouldThrowExceptionWhenDisplayNameIsNullAndDisplayVersionIsNotNull() {
        assertThatThrownBy(() -> displayFieldsValidator.validateDisplayNameDisplayVersion(null, "1.0", "DomainObjectType", "name"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Display name: 'null'" + " must not be blank for DomainObjectType with id:'name'");

    }

    @Test
    void validateDisplayNameDisplayVersion_shouldDoNothingWhenDisplayNameIsNotNullAndDisplayVersionIsNull() {
        assertThatNoException().isThrownBy(() -> displayFieldsValidator.validateDisplayNameDisplayVersion("text", null, "DomainObjectType", "name"));
    }

    @Test
    void validateDisplayNameDisplayVersion_shouldDoNothingWhenDisplayNameIsNotNullAndDisplayVersionIsNotNull() {
        assertThatNoException().isThrownBy(() -> displayFieldsValidator.validateDisplayNameDisplayVersion("text", "1.0", "DomainObjectType", "name"));
    }

}