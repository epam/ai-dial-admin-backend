package com.epam.aidial.cfg.configuration.logging;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdInterceptorTest {

    private CorrelationIdInterceptor interceptor;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private OpenTelemetrySdk openTelemetrySdk;

    @BeforeEach
    void setUp() {
        interceptor = new CorrelationIdInterceptor();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        
        // Set up minimal OpenTelemetry SDK for testing
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder().build();
        
        openTelemetrySdk = OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .build();
        
        // Set as global instance for the test
        GlobalOpenTelemetry.set(openTelemetrySdk);
    }

    @AfterEach
    void tearDown() {
        // Clean up OpenTelemetry
        if (openTelemetrySdk != null) {
            openTelemetrySdk.close();
        }
        GlobalOpenTelemetry.resetForTest();
    }

    @Test
    void testPreHandle_setsTraceParentHeader_whenOpenTelemetryContextExists() {
        // given - create a span with OpenTelemetry
        Tracer tracer = openTelemetrySdk.getTracer("test");
        Span span = tracer.spanBuilder("test-span").startSpan();
        
        try (Scope scope = span.makeCurrent()) {
            // when
            boolean result = interceptor.preHandle(request, response, null);

            // then
            assertThat(result).isTrue();
            String traceParent = response.getHeader(CorrelationIdInterceptor.TRACEPARENT_HEADER_NAME);
            assertThat(traceParent).isNotNull();
            assertThat(traceParent).matches("^00-[0-9a-f]{32}-[0-9a-f]{16}-[0-9a-f]{2}$");
            
            // Verify format: 00-{trace-id}-{span-id}-{trace-flags}
            String[] parts = traceParent.split("-");
            assertThat(parts).hasSize(4);
            assertThat(parts[0]).isEqualTo("00"); // version
            assertThat(parts[1]).hasSize(32); // trace-id (32 hex chars)
            assertThat(parts[2]).hasSize(16); // span-id (16 hex chars)
            assertThat(parts[3]).hasSize(2); // trace-flags (2 hex chars)
        } finally {
            span.end();
        }
    }

    @Test
    void testPreHandle_noTraceParentHeader_whenNoOpenTelemetryContext() {
        // given - no active span context
        
        // when
        boolean result = interceptor.preHandle(request, response, null);

        // then
        assertThat(result).isTrue();
        // When no OpenTelemetry context is available, traceparent may be null
        // This is acceptable behavior - verify it's not set or null
        String traceParent = response.getHeader(CorrelationIdInterceptor.TRACEPARENT_HEADER_NAME);
        // verify traceParent can be null when no OpenTelemetry context exists, which is expected
        // and interceptor should not throw exceptions even when trace context is unavailable
        assertThat(traceParent).isNullOrEmpty();
    }
}
