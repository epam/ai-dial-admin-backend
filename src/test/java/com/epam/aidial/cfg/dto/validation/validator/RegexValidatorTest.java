package com.epam.aidial.cfg.dto.validation.validator;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RegexValidatorTest {

    private RegexValidator validator;

    @BeforeEach
    void init() {
        validator = new RegexValidator();
    }

    @ParameterizedTest
    @CsvSource(value = {"null", "''", "' '", "[A-Za-z0-9]", "http://qwe.com", "/abc/myendpoint/.*", "https?://\\S*"}, nullValues = "null")
    void testIsValid_shouldReturnTrueWhenValidRegex(String regex) {
        var result = validator.isValid(regex, null);
        Assertions.assertThat(result).isTrue();
    }

    @ParameterizedTest
    @CsvSource({"URL???", "[", "{", "(", ")"})
    void testIsValid_shouldReturnFalseWhenInvalidRegex(String regex) {
        var result = validator.isValid(regex, null);
        Assertions.assertThat(result).isFalse();
    }

}