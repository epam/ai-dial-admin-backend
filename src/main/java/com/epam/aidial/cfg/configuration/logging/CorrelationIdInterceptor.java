package com.epam.aidial.cfg.configuration.logging;

import com.epam.aidial.cfg.utils.TraceContextUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor that sets trace context headers in HTTP responses.
 * Uses OpenTelemetry trace IDs exclusively via W3C Trace Context standard.
 */
@Slf4j
public class CorrelationIdInterceptor implements HandlerInterceptor {
    public static final String TRACEPARENT_HEADER_NAME = "traceparent";

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response,
                             final Object handler) {
        // Set W3C Trace Context header for interoperability
        String traceParent = TraceContextUtils.formatTraceParent();
        if (traceParent != null) {
            response.setHeader(TRACEPARENT_HEADER_NAME, traceParent);
        }
        
        return true;
    }

    @Override
    public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response,
                                final Object handler, final Exception ex) {
        // No cleanup needed
    }
}
