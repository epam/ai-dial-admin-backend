package com.epam.aidial.cfg.configuration.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdInterceptorTest {

    private CorrelationIdInterceptor interceptor;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        interceptor = new CorrelationIdInterceptor();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @ParameterizedTest
    @MethodSource("validCorrelationIds")
    void testPreHandle_withValidCorrelationId(String validCorrelationId) {
        // given
        request.addHeader(CorrelationIdInterceptor.CORRELATION_ID_HEADER_NAME, validCorrelationId);

        // when
        boolean result = interceptor.preHandle(request, response, null);

        // then
        assertThat(result).isTrue();
        assertThat(response.getHeader(CorrelationIdInterceptor.CORRELATION_ID_HEADER_NAME))
                .isEqualTo(validCorrelationId);
        assertThat(MDC.get("_correlation_id")).isEqualTo(validCorrelationId);
    }

    private static Stream<Arguments> validCorrelationIds() {
        return Stream.of(
                Arguments.of("abcdefghijklmnop"), // 16 alphanumeric characters
                Arguments.of("abcdefghijklmnopqrstuvwxyz123456") // 32 alphanumeric characters
        );
    }

    @ParameterizedTest
    @MethodSource("invalidCorrelationIds")
    void testPreHandle_withInvalidCorrelationId(String invalidCorrelationId) {
        // given
        request.addHeader(CorrelationIdInterceptor.CORRELATION_ID_HEADER_NAME, invalidCorrelationId);

        // when
        boolean result = interceptor.preHandle(request, response, null);

        // then
        assertThat(result).isTrue();
        String actualCorrelationId = response.getHeader(CorrelationIdInterceptor.CORRELATION_ID_HEADER_NAME);
        assertThat(actualCorrelationId).isNotEqualTo(invalidCorrelationId);
        assertThat(actualCorrelationId).matches("^[a-zA-Z0-9]{16,32}$");
        assertThat(MDC.get("_correlation_id")).isEqualTo(actualCorrelationId);
    }


    private static Stream<Arguments> invalidCorrelationIds() {
        return Stream.of(
                Arguments.of("short"), // too short
                Arguments.of("invalid-correlation-id!") // contains invalid characters
        );
    }

    @Test
    void testPreHandle_withMissingCorrelationId() {
        // given - no header set

        // when
        boolean result = interceptor.preHandle(request, response, null);

        // then
        assertThat(result).isTrue();
        String actualCorrelationId = response.getHeader(CorrelationIdInterceptor.CORRELATION_ID_HEADER_NAME);
        assertThat(actualCorrelationId).isNotNull();
        assertThat(actualCorrelationId).matches("^[a-zA-Z0-9]{16,32}$");
        assertThat(MDC.get("_correlation_id")).isEqualTo(actualCorrelationId);
    }

    @Test
    void testPreHandle_withCorrelationIdContainingCrLfInMiddle() {
        // given
        String maliciousCorrelationId = "abc\r\ndefghijklmnop"; // contains CRLF in middle
        request.addHeader(CorrelationIdInterceptor.CORRELATION_ID_HEADER_NAME, maliciousCorrelationId);

        // when
        boolean result = interceptor.preHandle(request, response, null);

        // then
        assertThat(result).isTrue();
        String actualCorrelationId = response.getHeader(CorrelationIdInterceptor.CORRELATION_ID_HEADER_NAME);
        // After sanitization, CRLF is removed, but the value is still invalid (doesn't match pattern)
        // So a new correlation ID should be generated
        assertThat(actualCorrelationId).isNotEqualTo(maliciousCorrelationId);
        assertThat(actualCorrelationId).doesNotContain("\r");
        assertThat(actualCorrelationId).doesNotContain("\n");
        assertThat(actualCorrelationId).matches("^[a-zA-Z0-9]{16,32}$");
        assertThat(MDC.get("_correlation_id")).isEqualTo(actualCorrelationId);
    }
}

