package com.epam.aidial.cfg.domain.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EndpointValidatorTest {

    @Test
    void isValidUrlPath_shouldReturnTrueForSimplePaths() {
        assertTrue(EndpointValidator.isValidUrlPath("/api"));
        assertTrue(EndpointValidator.isValidUrlPath("/"));
        assertTrue(EndpointValidator.isValidUrlPath("/v1"));
    }

    @Test
    void isValidUrlPath_shouldReturnTrueForNestedPaths() {
        assertTrue(EndpointValidator.isValidUrlPath("/api/123"));
        assertTrue(EndpointValidator.isValidUrlPath("/api/"));
    }

    @Test
    void isValidUrlPath_shouldReturnTrueForPathsWithoutLeadingSlash() {
        assertTrue(EndpointValidator.isValidUrlPath("api"));
        assertTrue(EndpointValidator.isValidUrlPath("somePath/"));
    }

    @Test
    void isValidUrlPath_shouldReturnTrueForPathsWithTrailingSlash() {
        assertTrue(EndpointValidator.isValidUrlPath("/somePath/"));
    }

    @Test
    void isValidUrlPath_shouldReturnTrueForPathsWithQueryParameters() {
        assertTrue(EndpointValidator.isValidUrlPath("/somePath?param1=val1;param2=val2"));
    }

    @Test
    void isValidUrlPath_shouldReturnTrueForNullPath() {
        assertTrue(EndpointValidator.isValidUrlPath(null));
    }

    @Test
    void isValidUrlPath_shouldReturnTrueForEmptyPath() {
        assertTrue(EndpointValidator.isValidUrlPath(""));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/invalid path with spaces",
            "/invalid@path",
            "/path#with#hash",
            "/path$with$dollar",
            "/path\\with\\backslash",
            "/path:with:colon",
            "/path<with>brackets"
    })
    void isValidUrlPath_shouldReturnFalseForInvalidPaths(String path) {
        assertFalse(EndpointValidator.isValidUrlPath(path));
    }

    @Test
    void isInvalidUrlPath_shouldReturnFalseForValidPaths() {
        assertFalse(EndpointValidator.isInvalidUrlPath("/api"));
        assertFalse(EndpointValidator.isInvalidUrlPath("/"));
        assertFalse(EndpointValidator.isInvalidUrlPath("api"));
    }

    @Test
    void isInvalidUrlPath_shouldReturnTrueForInvalidPaths() {
        assertTrue(EndpointValidator.isInvalidUrlPath("/invalid path"));
    }
}