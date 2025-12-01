package com.epam.aidial.cfg.utils;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import lombok.experimental.UtilityClass;

/**
 * Utility class for extracting trace context information from OpenTelemetry.
 */
@UtilityClass
public class TraceContextUtils {

    private static final String INVALID_TRACE_ID = "00000000000000000000000000000000";
    private static final String INVALID_SPAN_ID = "0000000000000000";

    /**
     * Gets the current trace ID from OpenTelemetry span context.
     *
     * @return trace ID as hex string, or null if not available
     */
    public static String getTraceId() {
        Span currentSpan = Span.current();
        SpanContext spanContext = currentSpan.getSpanContext();
        String traceId = spanContext.getTraceId();

        if (!spanContext.isValid() || INVALID_TRACE_ID.equals(traceId)) {
            return null;
        }

        return traceId;
    }

    /**
     * Gets the current span ID from OpenTelemetry span context.
     *
     * @return span ID as hex string, or null if not available
     */
    public static String getSpanId() {
        Span currentSpan = Span.current();
        SpanContext spanContext = currentSpan.getSpanContext();
        String spanId = spanContext.getSpanId();

        if (!spanContext.isValid() || INVALID_SPAN_ID.equals(spanId)) {
            return null;
        }

        return spanId;
    }

    /**
     * Formats W3C Trace Context traceparent header value.
     * Format: 00-{trace-id}-{span-id}-{trace-flags}
     *
     * @return W3C traceparent header value or null if trace context is invalid
     */
    public static String formatTraceParent() {
        Span currentSpan = Span.current();
        SpanContext spanContext = currentSpan.getSpanContext();

        if (!spanContext.isValid()) {
            return null;
        }

        String traceId = spanContext.getTraceId();
        String spanId = spanContext.getSpanId();
        TraceFlags traceFlags = spanContext.getTraceFlags();

        if (INVALID_TRACE_ID.equals(traceId) || INVALID_SPAN_ID.equals(spanId)) {
            return null;
        }

        // W3C Trace Context format: version-trace-id-parent-id-trace-flags
        // version is always "00" (current version)
        // trace-flags is 2 hex characters (01 = sampled, 00 = not sampled)
        String flags = String.format("%02x", traceFlags.asByte() & 0xFF);

        return String.format("00-%s-%s-%s", traceId, spanId, flags);
    }
}