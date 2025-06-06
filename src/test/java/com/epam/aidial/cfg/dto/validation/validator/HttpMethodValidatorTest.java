package com.epam.aidial.cfg.dto.validation.validator;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class HttpMethodValidatorTest {

    private HttpMethodValidator validator;

    @BeforeEach
    void init() {
        validator = new HttpMethodValidator();
    }

    @ParameterizedTest
    @MethodSource("validMethods")
    void testIsValid_Success(String method) {
        var result = validator.isValid(method, null);
        Assertions.assertThat(result).isTrue();
    }

    @ParameterizedTest
    @MethodSource("notValidMethods")
    void testIsValid_IsNotValid(String method) {
        var result = validator.isValid(method, null);
        Assertions.assertThat(result).isFalse();
    }

    private static Stream<Arguments> validMethods() {
        return Stream.of(
                Arguments.of("GET"),
                Arguments.of("PUT"),
                Arguments.of("DELETE")
        );
    }

    private static Stream<Arguments> notValidMethods() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of((String) null),
                Arguments.of("TEST")
        );
    }
}