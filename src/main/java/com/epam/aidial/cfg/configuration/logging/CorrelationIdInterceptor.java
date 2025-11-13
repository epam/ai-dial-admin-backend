package com.epam.aidial.cfg.configuration.logging;

import com.epam.aidial.cfg.utils.HeaderUtils;
import io.opentelemetry.api.trace.Span;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.regex.Pattern;

@Slf4j
public class CorrelationIdInterceptor implements HandlerInterceptor {
    public static final String CORRELATION_ID_HEADER_NAME = "X-Correlation-Id";
    private static final Pattern CORRELATION_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9]{16,32}$");
    private static final String NOT_VALID_CORRELATION_ID = "00000000000000000000000000000000";
    private static final int CORRELATION_ID_LENGTH = 16;

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response,
                             final Object handler) {
        response.setHeader(CORRELATION_ID_HEADER_NAME, resolveValidCorrelationId(request));
        return true;
    }

    @Override
    public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response,
                                final Object handler, final Exception ex) {
        // do nothing
    }

    private String resolveValidCorrelationId(final HttpServletRequest request) {
        var correlationId = request.getHeader(CORRELATION_ID_HEADER_NAME);
        correlationId = HeaderUtils.sanitizeHeaderValue(correlationId);
        var validCorrelationId = (correlationId != null && CORRELATION_ID_PATTERN.matcher(correlationId).matches())
                ? correlationId : generateCorrelationId();
        MDC.put("_correlation_id", validCorrelationId);
        if (!validCorrelationId.equals(correlationId)) {
            var uri = request.getRequestURI();
            var message = "CorrelationId ='" + StringEscapeUtils.escapeJava(correlationId)
                    + "' isn't valid, generated CorrelationId='" + validCorrelationId + "', url='" + uri + "'";
            if (StringUtils.isBlank(correlationId)) {
                log.debug(message);
            } else {
                log.error(message);
            }
        }
        return validCorrelationId;
    }

    public static String generateCorrelationId() {
        String traceId = Span.current().getSpanContext().getTraceId();
        if (NOT_VALID_CORRELATION_ID.equals(traceId)) {
            return generateRandomCorrelationId();
        }
        return traceId;
    }

    private static String generateRandomCorrelationId() {
        return RandomStringUtils.randomAlphanumeric(CORRELATION_ID_LENGTH);
    }
}
