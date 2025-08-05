package com.epam.aidial.cfg.domain.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EndpointValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "http://dial-interceptor-example.dial-development/openai/deployments/image-watermark/chat/completions",
            "http://dial-interceptor-example.com/openai/deployments/image-watermark/chat/completions",
            "http://dial-interceptor-example.ru/openai/deployments/image-watermark/chat/completions",
            "http://dial-interceptor-example.local/openai/deployments/image-watermark/chat/completions",
            "http://localhost:8080/test",
            "http://example.local",
            "http://127.0.0.1:8080/test",
            "https://www.google.com",
            "http://sub.example.local",
            "http://example.dial-dev"
    })
    void isValidUrl_shouldReturnTrue(String url) {
        assertTrue(EndpointValidator.isValidUrl(url), "Expected URL to be valid: " + url);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://dial-interceptor-example.loc$*a=l/openai/deployments/image-watermark/chat/completions",
            "http://invalid-url=$",
            "http://invalid-url/==",
            "http://example.com:invalidport",
            "http://example.local:999999",
            "http://dial-interceptor-example.local\\openai",
            "ftp://example.local",
            "http://",
            "http://invalid_domain.com",
            "http://example.com:-80"
    })
    void isInvalidUrl_shouldReturnTrue(String url) {
        assertTrue(EndpointValidator.isInvalidUrl(url), "Expected URL to be invalid: " + url);
    }

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
    void isValidUrlPath_shouldReturnFalseForPathsWithQueryParameters() {
        assertFalse(EndpointValidator.isValidUrlPath("/somePath?param1=val1"));
        assertFalse(EndpointValidator.isValidUrlPath("/somePath?param1=val1;param2=val2"));
        assertFalse(EndpointValidator.isValidUrlPath("/api/v1?key=value"));
    }

    @Test
    void isValidUrlPath_shouldReturnFalseForNullPath() {
        assertFalse(EndpointValidator.isValidUrlPath(null));
    }

    @Test
    void isValidUrlPath_shouldReturnFalseForEmptyPath() {
        assertFalse(EndpointValidator.isValidUrlPath(""));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/invalid path with spaces",
            "/invalid@path",
            "/path#with#hash",
            "/path$with$dollar",
            "/path\\with\\backslash",
            "/path:with:colon",
            "/path<with>brackets",
            "/path?with=queryparams",
            "/path;with;semicolons",
            "/path=with=equals"
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