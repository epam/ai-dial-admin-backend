package com.epam.aidial.cfg.web.handler;

import com.epam.aidial.cfg.utils.TraceContextUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 * Error response view that includes trace information for distributed tracing.
 * Created by Aliaksei Kurnosau on 9/9/24.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorView {

    private String path;
    private String method;
    private Integer status;
    private String error;
    private String message;

    /**
     * W3C Trace Context traceparent value.
     * Format: 00-{trace-id}-{span-id}-{trace-flags}
     * Example: 00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01
     * This enables distributed tracing correlation across services.
     */
    private String traceparent;

    public ErrorView(HttpServletRequest request, HttpStatus status, String errorMessage) {
        this.path = request.getServletPath();
        this.method = request.getMethod();
        this.status = status.value();
        this.error = status.getReasonPhrase();
        this.message = errorMessage;

        // Populate trace information from OpenTelemetry context
        this.traceparent = TraceContextUtils.formatTraceParent();
    }
}