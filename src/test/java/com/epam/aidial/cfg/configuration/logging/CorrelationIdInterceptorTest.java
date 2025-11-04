package com.epam.aidial.cfg.configuration.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

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

    @Test
    void testPreHandle_withValidCorrelationId() {
        // given
        String validCorrelationId = "abcdefghijklmnop"; // 16 alphanumeric characters
        request.addHeader(CorrelationIdInterceptor.CORRELATION_ID_HEADER_NAME, validCorrelationId);

        // when
        boolean result = interceptor.preHandle(request, response, null);

        // then
        assertThat(result).isTrue();
        assertThat(response.getHeader(CorrelationIdInterceptor.CORRELATION_ID_HEADER_NAME))
                .isEqualTo(validCorrelationId);
        assertThat(MDC.get("_correlation_id")).isEqualTo(validCorrelationId);
    }

    @Test
    void testPreHandle_withValidCorrelationId_32Characters() {
        // given
        String validCorrelationId = "abcdefghijklmnopqrstuvwxyz123456"; // 32 alphanumeric characters
        request.addHeader(CorrelationIdInterceptor.CORRELATION_ID_HEADER_NAME, validCorrelationId);

        // when
        boolean result = interceptor.preHandle(request, response, null);

        // then
        assertThat(result).isTrue();
        assertThat(response.getHeader(CorrelationIdInterceptor.CORRELATION_ID_HEADER_NAME))
                .isEqualTo(validCorrelationId);
        assertThat(MDC.get("_correlation_id")).isEqualTo(validCorrelationId);
    }

    @Test
    void testPreHandle_withInvalidCorrelationId_tooShort() {
        // given
        String invalidCorrelationId = "short"; // too short
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

    @Test
    void testPreHandle_withInvalidCorrelationId_invalidCharacters() {
        // given
        String invalidCorrelationId = "invalid-correlation-id!"; // contains invalid characters
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
    void testPreHandle_withCorrelationIdContainingCRLFInMiddle() {
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

