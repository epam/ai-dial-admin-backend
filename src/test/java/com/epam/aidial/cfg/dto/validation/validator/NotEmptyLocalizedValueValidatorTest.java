package com.epam.aidial.cfg.dto.validation.validator;

import com.epam.aidial.cfg.dto.LocalizedValueDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

class NotEmptyLocalizedValueValidatorTest {

    private NotEmptyLocalizedValueValidator validator;

    @BeforeEach
    void init() {
        validator = new NotEmptyLocalizedValueValidator();
    }

    @Test
    void testIsValid_shouldReturnFalseWhenNull() {
        var result = validator.isValid(null, null);
        Assertions.assertThat(result).isFalse();
    }

    @ParameterizedTest
    @MethodSource("validValues")
    void testIsValid_shouldReturnTrueForNonEmptyValues(LocalizedValueDto value) {
        var result = validator.isValid(value, null);
        Assertions.assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("invalidValues")
    void testIsValid_shouldReturnFalseForEmptyValues(LocalizedValueDto value) {
        var result = validator.isValid(value, null);
        Assertions.assertThat(result).isFalse();
    }

    private static Stream<Arguments> validValues() {
        return Stream.of(
                Arguments.of(LocalizedValueDto.of("GPT-4")),
                Arguments.of(LocalizedValueDto.of(Map.of("en", "GPT-4"))),
                Arguments.of(LocalizedValueDto.of(Map.of("en", " ", "fr", "GPT-4")))
        );
    }

    private static Stream<Arguments> invalidValues() {
        return Stream.of(
                Arguments.of(LocalizedValueDto.of("")),
                Arguments.of(LocalizedValueDto.of(" ")),
                Arguments.of(LocalizedValueDto.of(Map.of())),
                Arguments.of(LocalizedValueDto.of(Map.of("en", ""))),
                Arguments.of(LocalizedValueDto.of(Map.of("en", " ", "fr", "")))
        );
    }
}
