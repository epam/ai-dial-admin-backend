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
        assertThatThrownBy(() -> idFieldValidator.validateName("DomainObjectType", name))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("DomainObjectType name must not be empty");
    }

    @ParameterizedTest
    @ValueSource(strings = {"nameWith%", "nameWith;", "nameWith/", "nameWith\\"})
    void validateName_shouldThrowExceptionWhenNameWithIllegalCharacters(String name) {
        assertThatThrownBy(() -> idFieldValidator.validateName("DomainObjectType", name))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Illegal characters found in DomainObjectType name: " + name);
    }

    @Test
    void validateName_shouldNotThrowExceptionWhenNameWithLegalCharacters() {
        assertThatCode(() -> idFieldValidator.validateName("DomainObjectType", "name With ~!@#$^&*()-_=+[]{}:'\",<.>?| and some unicode 😇 Ą Ŏ"))
                .doesNotThrowAnyException();
    }

    @Test
    void validateId_shouldThrowExceptionWhenNullId() {
        assertThatThrownBy(() -> idFieldValidator.validateId("DomainObjectType", null, "idField"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("DomainObjectType idField must not be empty");
    }

    @Test
    void validateName_shouldNotThrowExceptionWhenValidId() {
        assertThatCode(() -> idFieldValidator.validateId("DomainObjectType", new Object(), "idField"))
                .doesNotThrowAnyException();
    }
}