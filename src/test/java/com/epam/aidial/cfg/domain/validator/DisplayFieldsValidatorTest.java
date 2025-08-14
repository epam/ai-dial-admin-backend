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
        assertThatThrownBy(() -> displayFieldsValidator.validateDisplayNameDisplayVersion(null, displayVersion))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid display version: '" + displayVersion + "'");
    }

    @Test
    void validateDisplayNameDisplayVersion_shouldThrowExceptionWhenDisplayNameIsNullAndDisplayVersionIsNotNull() {
        assertThatThrownBy(() -> displayFieldsValidator.validateDisplayNameDisplayVersion(null, "1.0"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Display version: '1.0' can not be specified without display name");

    }

    @Test
    void validateDisplayNameDisplayVersion_shouldDoNothingWhenDisplayNameIsNotNullAndDisplayVersionIsNull() {
        assertThatNoException().isThrownBy(() -> displayFieldsValidator.validateDisplayNameDisplayVersion("text", null));
    }

    @Test
    void validateDisplayNameDisplayVersion_shouldDoNothingWhenDisplayNameIsNullAndDisplayVersionIsNull() {
        assertThatNoException().isThrownBy(() -> displayFieldsValidator.validateDisplayNameDisplayVersion(null, null));
    }

    @Test
    void validateDisplayNameDisplayVersion_shouldDoNothingWhenDisplayNameIsNotNullAndDisplayVersionIsNotNull() {
        assertThatNoException().isThrownBy(() -> displayFieldsValidator.validateDisplayNameDisplayVersion("text", "1.0"));
    }

}