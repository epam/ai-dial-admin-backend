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
        assertThatThrownBy(() -> displayFieldsValidator.validateDisplayNameDisplayVersion(displayName, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid display name: '" + displayName + "'");

    }

    @ParameterizedTest
    @CsvSource({"''", "' '"})
    void validateDisplayNameDisplayVersion_shouldThrowExceptionWhenDisplayVersionIsBlank(String displayVersion) {
        assertThatThrownBy(() -> displayFieldsValidator.validateDisplayNameDisplayVersion("test", displayVersion))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid display version: '" + displayVersion + "'");
    }

    @ParameterizedTest
    @CsvSource(value = {"null", "''", "' '"}, nullValues = "null")
    void validateDisplayName_shouldThrowExceptionWhenDisplayNameIsNullOrBlank(String displayName) {
        assertThatThrownBy(() -> displayFieldsValidator.validateDisplayName(displayName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid display name: '" + displayName + "'");
    }

    @Test
    void validateDisplayNameDisplayVersion_shouldThrowExceptionWhenDisplayNameIsNullAndDisplayVersionIsNotNull() {
        assertThatThrownBy(() -> displayFieldsValidator.validateDisplayNameDisplayVersion(null, "1.0"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid display name: 'null'");

    }

    @Test
    void validateDisplayNameDisplayVersion_shouldDoNothingWhenDisplayNameIsNotNullAndDisplayVersionIsNull() {
        assertThatNoException().isThrownBy(() -> displayFieldsValidator.validateDisplayNameDisplayVersion("text", null));
    }

    @Test
    void validateDisplayNameDisplayVersion_shouldDoNothingWhenDisplayNameIsNotNullAndDisplayVersionIsNotNull() {
        assertThatNoException().isThrownBy(() -> displayFieldsValidator.validateDisplayNameDisplayVersion("text", "1.0"));
    }

}