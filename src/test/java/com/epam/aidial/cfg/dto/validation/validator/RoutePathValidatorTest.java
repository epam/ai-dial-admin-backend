package com.epam.aidial.cfg.dto.validation.validator;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RoutePathValidatorTest {

    private RoutePathValidator validator;

    @BeforeEach
    void init() {
        validator = new RoutePathValidator();
    }

    @Test
    void testIsValid_shouldReturnFalseForNull() {
        var result = validator.isValid(null, null);
        Assertions.assertThat(result).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"   ", "\t", "\n", "  \t  ", "\r\n", " ", ""})
    void testIsValid_shouldReturnFalseForBlankStrings(String blankString) {
        var result = validator.isValid(blankString, null);
        Assertions.assertThat(result).isFalse();
    }

    @Test
    void testIsValid_shouldReturnFalseForMaxLengthExceeded() {
        var longPath = "/" + "a".repeat(4096);
        var result = validator.isValid(longPath, null);
        Assertions.assertThat(result).isFalse();
    }

    @Test
    void testIsValid_shouldReturnTrueForMaxLength() {
        var longPath = "/" + "a".repeat(4095);
        var result = validator.isValid(longPath, null);
        Assertions.assertThat(result).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/",
            "/api",
            "/api/v1",
            "/v1/endpoint",
            "/api/v1/endpoint/path",
            "/api-v1",
            "/api_v1",
            "/api.v1",
            "/123",
            "/api123",
            "/a1-b2_c3.d4"
    })
    void testIsValid_shouldReturnTrueForValidPlainPaths(String path) {
        var result = validator.isValid(path, null);
        Assertions.assertThat(result).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/v1/vector_store(/[^/]+)*$",
            "/.*",
            "/api/.*",
            "/v1/.*/endpoint",
            "/api/v1/[0-9]+",
            "/service/(test|demo)",
            "^/api/v1$",
            "/path/.*\\.json$",
            "/v1/vector_store(/[^/]+)*$"
    })
    void testIsValid_shouldReturnTrueForValidRegexPatterns(String regex) {
        var result = validator.isValid(regex, null);
        Assertions.assertThat(result).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "URL???",
            "[",
            "{",
            "(",
            ")",
            "/api/[unclosed",
            "/api/(unclosed",
            "/api/{unclosed",
            "/api/unopened]",
            "/api/unopened)",
            "/api/unopened}",
            ".*[",
            "api/v1.*" //should start with `/`
    })
    void testIsValid_shouldReturnFalseForInvalidRegexPatterns(String invalidRegex) {
        var result = validator.isValid(invalidRegex, null);
        Assertions.assertThat(result).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/invalid path with spaces",
            "/invalid@path",
            "/path#with#hash",
            "/path:with:colon",
            "/api//v1",
            "no-leading-slash",
            "/api&test",
            "/api<test",
            "/api>test",
            "/api=test",
            "/api!test"

    })
    void testIsValid_shouldReturnFalseForInvalidUrlPaths(String invalidPath) {
        var result = validator.isValid(invalidPath, null);
        Assertions.assertThat(result).isFalse();
    }
}

