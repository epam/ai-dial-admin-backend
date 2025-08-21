package com.epam.aidial.cfg.web.handler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Created by Aliaksei Kurnosau on 9/9/24.
 */
@Data
@NoArgsConstructor
public class ErrorView {

    private String path;
    private String method;
    private Integer status;
    private String error;
    private String message;

    public ErrorView(HttpServletRequest request, HttpStatus status, String errorMessage) {

        this.path = request.getServletPath();
        this.method = request.getMethod();
        this.status = status.value();
        this.error = status.getReasonPhrase();
        this.message = errorMessage;
    }
}
