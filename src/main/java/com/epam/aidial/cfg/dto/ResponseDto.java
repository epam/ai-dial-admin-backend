package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.dto.validation.annotation.HttpStatusCode;
import lombok.Data;

@Data
public class ResponseDto {
    @HttpStatusCode
    private int status;
    private String body;

}
