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
    void testIsValid_shouldReturnTrueForEmptyString() {
        var result = validator.isValid("", null);
        Assertions.assertThat(result).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"   ", "\t", "\n", "  \t  ", "\r\n"})
    void testIsValid_shouldReturnFalseForBlankStrings(String blankString) {
        var result = validator.isValid(blankString, null);
        Assertions.assertThat(result).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/api/v1",
            "/api/v1/",
            "/service/",
            "/",
            "api/v1",
            "/v1/endpoint",
            "/api/v1/endpoint/path"
    })
    void testIsValid_shouldReturnTrueForValidRelativeUrls(String url) {
        var result = validator.isValid(url, null);
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
            "[unclosed",
            "(unclosed",
            ".*[",
            "/api/[invalid"
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
            "/path:with:colon"
    })
    void testIsValid_shouldReturnFalseForInvalidUrlPaths(String invalidPath) {
        var result = validator.isValid(invalidPath, null);
        Assertions.assertThat(result).isFalse();
    }
}

