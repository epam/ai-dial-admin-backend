package com.epam.aidial.cfg.dto.validation.annotation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

class UpstreamEndpointTest {

    private Validator validator;

    @BeforeEach
    void init() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @ParameterizedTest
    @MethodSource("validEndpoints")
    void testIsValid_Success(String endpoint) {
        TestClass testClass = new TestClass(endpoint);
        Set<ConstraintViolation<TestClass>> violations = validator.validate(testClass);
        Assertions.assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidEndpoints")
    void testIsValid_IsNotValid(String endpoint) {
        TestClass testClass = new TestClass(endpoint);
        Set<ConstraintViolation<TestClass>> violations = validator.validate(testClass);
        Assertions.assertThat(violations).isNotEmpty();
    }

    @Test
    void testIsValid_violationHasCorrectMessageWhenEndpointIsNull() {
        TestClass testClass = new TestClass(null);
        Set<ConstraintViolation<TestClass>> violations = validator.validate(testClass);
        Assertions.assertThat(violations).isNotEmpty();
        ConstraintViolation<TestClass> violation = violations.iterator().next();
        Assertions.assertThat(violation.getMessage()).isEqualTo("Upstream endpoint must not be null.");
    }

    private static Stream<Arguments> validEndpoints() {
        return Stream.of(
                Arguments.of("http://example.com"),
                Arguments.of("https://example.com"),
                Arguments.of("http://localhost:8080"),
                Arguments.of("http://127.0.0.1:8080"),
                Arguments.of("http://ai-test:50/"),
                Arguments.of("http://ai-test:50/api"),
                Arguments.of("http://ai-test:50/api?param=value"),
                Arguments.of("http://ai-test/api")
        );
    }

    private static Stream<Arguments> invalidEndpoints() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of((String) null),
                Arguments.of("ftp://example.com"),
                Arguments.of("example.com"),
                Arguments.of("http://")
        );
    }

    private record TestClass(@UpstreamEndpoint String endpoint) { }
}