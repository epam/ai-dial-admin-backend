package com.epam.aidial.cfg.domain.model;

import com.epam.aidial.cfg.dto.validation.annotation.HttpStatusCode;
import lombok.Data;

@Data
public class Response {
    @HttpStatusCode
    private int status;
    private String body;
}
