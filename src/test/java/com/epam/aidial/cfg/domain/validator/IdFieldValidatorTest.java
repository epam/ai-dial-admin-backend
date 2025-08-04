package com.epam.aidial.cfg.domain.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdFieldValidatorTest {

    private IdFieldValidator idFieldValidator;

    @BeforeEach
    void setUp() {
        idFieldValidator = new IdFieldValidator();
    }

    @ParameterizedTest
    @CsvSource(value = {"null", "''", "' '"}, nullValues = "null")
    void validateName_shouldThrowExceptionWhenEmptyName(String name) {
        assertThatThrownBy(() -> idFieldValidator.validateName(name))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name must not be empty");
    }

    @ParameterizedTest
    @ValueSource(strings = {"nameWith!", "nameWith@", "nameWith#", "nameWith$", "nameWith%", "nameWith^",
            "nameWith&", "nameWith*", "nameWith\\", "nameWith|", "nameWith/", "nameWith?"})
    void validateName_shouldThrowExceptionWhenNameWithIllegalCharacters(String name) {
        assertThatThrownBy(() -> idFieldValidator.validateName(name))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Illegal characters found in name");
    }

    @Test
    void validateName_shouldNotThrowExceptionWhenValidName() {
        assertThatCode(() -> idFieldValidator.validateName("name With A-Za-z0-9-_.,:;<>(){}[]"))
                .doesNotThrowAnyException();
    }

    @Test
    void validateId_shouldThrowExceptionWhenNullId() {
        assertThatThrownBy(() -> idFieldValidator.validateId(null, "idField"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("idField must not be empty");
    }

    @Test
    void validateName_shouldNotThrowExceptionWhenValidId() {
        assertThatCode(() -> idFieldValidator.validateId(new Object(), "idField"))
                .doesNotThrowAnyException();
    }
}